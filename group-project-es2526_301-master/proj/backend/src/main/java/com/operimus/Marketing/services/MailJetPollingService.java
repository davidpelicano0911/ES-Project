package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.entities.MailjetEventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service that periodically polls the MailJet API for email event updates.
 * 
 * This service automatically runs every 30 seconds and:
 * 1. Calls MailJet's /v3/REST/message endpoint to fetch message statuses
 * 2. Processes each message to extract status information
 * 3. Maps MailJet statuses to application event types
 * 4. Logs events to the database with proper lead/campaign context
 * 5. Deduplicates events to prevent duplicate entries
 * 
 * MailJet Message Statuses (mapped to EventType):
 * - "sent", "processed", "queued" → SENT
 * - "opened" → OPENED
 * - "clicked" → CLICKED
 * - "bounce", "softbounce", "hardbounce" → BOUNCED
 * - "spam" → SPAM
 * - "blocked" → BLOCKED
 * - "delivered" → DELIVERED
 * - "unsub" → UNSUBSCRIBED
 * 
 * Data Integrity:
 * - Events are only saved if a valid leadId is found from the SENT record
 * - Uses composite key (MessageID + EventType) for deduplication
 * - Orphaned events without leadId are logged but not saved
 * 
 * Authentication:
 * - Uses HTTP Basic Auth with MailJet API credentials from environment variables
 * - MAILJET_API_USERNAME (public key)
 * - MAILJET_API_PASSWORD (secret key)
 * 
 * Limitations:
 * - /v3/REST/message endpoint often doesn't return email addresses
 * - Falls back to looking up email from original SENT record
 * - Limited to 1000 messages per poll (MailJet API limit)
 * 
 * Performance:
 * - Runs on separate scheduler thread (doesn't block main application)
 * - Initial delay: 5 seconds after startup
 * - Poll interval: 30 seconds
 * 
 * Monitoring:
 * - Logs all polling cycles (start/end)
 * - Special logging for BLOCKED events with detailed metadata
 * - Error logging for API failures and processing exceptions
 * 
 * @see EmailLogService
 * @see MailjetEventType
 * @see EmailLog
 */
@Service
public class MailJetPollingService {

    private static final Logger logger = LoggerFactory.getLogger(MailJetPollingService.class);

    @Autowired
    private EmailLogService emailLogService;

    @Value("${mailjet.api-username:}")
    private String publicKey;

    @Value("${mailjet.api-password:}")
    private String privateKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String MAILJET_API_URL = "https://api.mailjet.com/v3/REST/message";
    private static final long POLL_INTERVAL = 30000; // 30 seconds

    /**
     * Scheduled task to poll MailJet for email events.
     * 
     * Execution Schedule:
     * - Runs every 30 seconds (POLL_INTERVAL)
     * - Initial delay: 5 seconds after application startup
     * - Executes on a separate scheduler thread (non-blocking)
     * 
     * Process Flow:
     * 1. Validates MailJet API credentials are configured
     * 2. Calls MailJet API: GET /v3/REST/message?Limit=1000&ShowSubject=true&ShowContactAlt=true
     * 3. Receives JSON response with up to 1000 messages and their statuses
     * 4. For each message:
     *    - Extracts ID, Status, ContactAlt (email), Subject, StateID
     *    - Maps status string to MailjetEventType enum
     *    - Checks if event already logged (deduplication)
     *    - Logs event if not previously recorded
     * 5. Tracks and logs statistics (processed count, blocked events count)
     * 
     * Error Handling:
     * - 401 Unauthorized: Logs authentication error with specific message
     * - 4xx/5xx errors: Logs full exception stack trace
     * - Graceful degradation: Errors don't stop the polling scheduler
     * 
     * Email Lookup Fallback:
     * - Primary: Uses ContactAlt field from MailJet response
     * - Fallback: Uses Email field if ContactAlt empty
     * - Last resort: Queries database for SENT record using messageId
     * 
     * Special Logging:
     * - BLOCKED events: Logged with ">>>" prefix and detailed metadata
     * - All events: DEBUG level by default, INFO for BLOCKED
     * - Cycle timing: Logs start and successful completion
     * 
     * @see #processMessage(JsonNode)
     * @see #mapStatusToEventType(String)
     * @see EmailLogService#logEvent(Long, MailjetEventType)
     */
    @Scheduled(fixedRate = POLL_INTERVAL, initialDelay = 5000)
    public void pollMailJetEvents() {
        try {
            logger.info("========== MailJet Polling Cycle Started ==========");
            
            // Skip if API credentials are not configured
            if (publicKey == null || publicKey.isEmpty() || privateKey == null || privateKey.isEmpty()) {
                logger.warn("MailJet credentials NOT configured - Cannot poll MailJet API");
                logger.warn("Public Key configured: {}", publicKey != null && !publicKey.isEmpty());
                logger.warn("Private Key configured: {}", privateKey != null && !privateKey.isEmpty());
                return;
            }
            
            logger.info("MailJet credentials are configured, proceeding with poll");
            logger.info("IMPORTANT: /v3/REST/message endpoint may not return email addresses in some cases");
            logger.info("For complete event data, ensure SENT events are logged first, or use webhook approach");

            // Poll messages from MailJet API
            // Endpoint: GET /v3/REST/message?Limit=1000
            // Returns all messages with their current status
            String url = MAILJET_API_URL + "?Limit=1000&ShowSubject=true&ShowContactAlt=true";
            logger.debug("Calling MailJet API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(publicKey, privateKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            try {
                // Use exchange() instead of getForObject() to properly pass headers
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
                );
                String response = responseEntity.getBody();
                logger.debug("MailJet API Response received (length: {} chars)", response != null ? response.length() : 0);
                JsonNode root = objectMapper.readTree(response);

                // Parse the response: { "Count": 100, "Data": [...], "Total": 1000 }
                if (root.has("Data")) {
                    JsonNode messages = root.get("Data");
                    if (messages.isArray()) {
                        logger.info("MailJet API returned {} messages", messages.size());
                        
                        int processedCount = 0;
                        int blockedCount = 0;
                        
                        for (JsonNode message : messages) {
                            if (processMessage(message)) {
                                processedCount++;
                                // Count blocked events specifically
                                if (message.has("Status")) {
                                    String status = message.get("Status").asText();
                                    if ("blocked".equalsIgnoreCase(status)) {
                                        blockedCount++;
                                    }
                                }
                            }
                        }
                        logger.info("MailJet Polling - Processed {} messages, {} BLOCKED events", processedCount, blockedCount);
                    } else {
                        logger.warn("'Data' field in MailJet response is not an array");
                    }
                } else {
                    logger.warn("MailJet response does not contain 'Data' field. Response keys: {}", 
                        new Iterator<String>() {
                            private Iterator<String> delegate = root.fieldNames();
                            @Override public boolean hasNext() { return delegate.hasNext(); }
                            @Override public String next() { return delegate.next(); }
                        });
                }
                logger.info("========== MailJet Polling Cycle Completed Successfully ==========");
                
            } catch (Exception e) {
                logger.error("Error polling MailJet API: {}", e.getMessage());
                logger.error("Exception details:", e);
                if (e.getMessage().contains("401")) {
                    logger.error("AUTHENTICATION ERROR (401) - MailJet credentials may be invalid");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in polling task: {}", e.getMessage(), e);
        }
    }

    /**
     * Process a single MailJet message and log its status event.
     * 
     * This is the core processing logic for individual messages from the MailJet API.
     * 
     * Input: MailJet API Message Object
     * MailJet returns messages in this format:
     * {
     *   "ID": 123456789,                // Unique message ID (required)
     *   "Subject": "Welcome Email",     // Email subject line
     *   "ContactAlt": "user@example.com", // Recipient email (often empty in API)
     *   "MessageStatus": 2,             // Numeric status code
     *   "Status": "sent",               // String status (required)
     *   "ContactID": 123456,            // MailJet contact ID
     *   "StateID": 19,                  // State code (19=blocked)
     *   "StatePermanent": true          // Whether state is permanent
     * }
     * 
     * Processing Steps:
     * 1. Extract required fields: ID, Status
     * 2. Extract optional fields: ContactAlt, Email, Subject, StateID
     * 3. Attempt email lookup (3 fallbacks):
     *    - Try ContactAlt from API response
     *    - Try Email field from API response
     *    - Query database for SENT record using messageId
     * 4. Map status string to MailjetEventType (e.g., "blocked" → BLOCKED)
     * 5. Check if event already logged (composite key: MessageID + EventType)
     * 6. If not logged, call emailLogService.logEvent()
     * 7. Event only saved if leadId is available
     * 
     * Special Handling for BLOCKED Events:
     * - Logged at INFO level with ">>>" prefix for visibility
     * - Includes MessageID, StateID, Status, Email, Subject
     * - Includes full EventLog details after save (LogID, LeadID, CampaignID)
     * 
     * Return Values:
     * - true: Event was successfully logged
     * - false: Event skipped (already exists, invalid, or no leadId)
     * 
     * MailJet API Response Structure from /v3/REST/message:
     * {
     *   "Count": 100,
     *   "Data": [
     *     { message object ... },
     *     { message object ... }
     *   ],
     *   "Total": 1000
     * }
     * 
     * NOTE: The /v3/REST/message endpoint frequently returns empty ContactAlt.
     * This is a known MailJet limitation. The fallback to database lookup
     * ensures we can still capture the email address.
     * 
     * @param message the message JSON node from MailJet API
     * @return true if event was successfully logged, false if skipped
     * 
     * @see #mapStatusToEventType(String)
     * @see EmailLogService#logEvent(Long, MailjetEventType)
     * @see EmailLogService#getEmailSendByMailjetMessageId(Long)
     */
    private boolean processMessage(JsonNode message) {
        try {
            // Extract fields from MailJet message response
            Long messageId = message.has("ID") ? message.get("ID").asLong() : null;
            
            // MessageStatus is the numeric status code
            Integer messageStatus = message.has("MessageStatus") ? message.get("MessageStatus").asInt() : null;
            
            // Status is the string representation (sent, opened, clicked, bounce, spam, blocked, etc.)
            String status = message.has("Status") ? message.get("Status").asText() : null;
            
            // Email might be in "ContactAlt" field or we need to query it separately
            String email = message.has("ContactAlt") ? message.get("ContactAlt").asText() : null;
            if (email == null || email.isEmpty()) {
                email = message.has("Email") ? message.get("Email").asText() : null;
            }
            
            // If still no email, try to get from database using the messageId (lookup from SENT record)
            if ((email == null || email.isEmpty()) && messageId != null) {
                Optional<EmailLog> sentEvent = emailLogService.getEmailSendByMailjetMessageId(messageId);
                if (sentEvent.isPresent()) {
                    email = sentEvent.get().getEmailAddress();
                    logger.debug("Retrieved email from SENT record: {} for MessageID: {}", email, messageId);
                }
            }
            
            String subject = message.has("Subject") ? message.get("Subject").asText() : null;
            Integer stateId = message.has("StateID") ? message.get("StateID").asInt() : null;

            if (messageId == null || status == null) {
                logger.debug("Message missing required fields: ID={}, Status={}", messageId, status);
                return false;
            }

            // Log detailed info for BLOCKED events
            boolean isBlocked = "blocked".equalsIgnoreCase(status);
            if (isBlocked) {
                logger.info(">>> BLOCKED MESSAGE DETECTED <<<");
                logger.info("    MessageID: {} | StateID: {}", messageId, stateId);
                logger.info("    Status: {} | MessageStatus Code: {}", status, messageStatus);
                logger.info("    ContactAlt (Email): {} | Subject: {}", email, subject);
                logger.info("    Note: Email from /v3/REST/message may be empty - use ContactID if needed");
            }

            logger.debug("Processing message: ID={}, Status={}, Email={}, StateID={}", messageId, status, email, stateId);

            // Map MailJet status to our enum
            MailjetEventType eventType = mapStatusToEventType(status);
            
            if (isBlocked) {
                logger.info("    Mapped Status '{}' to EventType: {}", status, eventType);
            } else {
                logger.debug("Mapped status '{}' to EventType: {}", status, eventType);
            }
            
            if (eventType != null) {
                // Check if this event (messageId + eventType) has already been logged
                // The deduplication key is: MessageID + EventType
                // This prevents logging the same event twice when the message status hasn't changed
                boolean alreadyExists = emailLogService.eventExists(messageId, eventType);
                
                if (isBlocked) {
                    logger.info("    Event Deduplication Check [MessageID={}, EventType={}]: Exists={}", 
                        messageId, eventType, alreadyExists);
                } else {
                    logger.debug("Event existence check: MessageID={}, EventType={}, Exists={}", messageId, eventType, alreadyExists);
                }
                
                if (alreadyExists) {
                    if (isBlocked) {
                        logger.info("    SKIPPING - Event [{}, {}] already logged previously", messageId, eventType);
                    } else {
                        logger.debug("Event already logged: MessageID={}, EventType={}", messageId, eventType);
                    }
                    return false;
                }
                
                // Log the event
                // Note: logEvent() will try to find the SENT record to copy lead/email info
                // If SENT record doesn't exist or no leadId is found, returns null (event not saved)
                EmailLog saved = emailLogService.logEvent(messageId, eventType);
                
                if (saved == null) {
                    if (isBlocked) {
                        logger.info("    SKIPPED - No leadId found for MessageID: {} | Cannot save event without leadId", messageId);
                    } else {
                        logger.debug("Event not saved: MessageID={}, EventType={} - No leadId available", messageId, eventType);
                    }
                    return false;
                }
                
                if (isBlocked) {
                    logger.info("    ✓ SUCCESSFULLY LOGGED BLOCKED EVENT");
                    logger.info("    LogID: {} | LeadID: {} | EmailAddress: {}", 
                        saved.getId(), saved.getLeadId(), saved.getEmailAddress());
                    logger.info("    CampaignID: {} | CreatedAt: {}", saved.getCampaignId(), saved.getCreatedAt());
                } else {
                    logger.debug("Logged message status: MessageID={}, Status={}, EventType={}, Email={}, LogID={}", 
                        messageId, status, eventType, email, saved.getId());
                }
                return true;
            } else {
                logger.debug("Unknown MailJet status: {}", status);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error processing message: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Maps MailJet message status strings to application MailjetEventType enum.
     * 
     * This provides a translation layer between MailJet's status naming and
     * the application's internal event type classification.
     * 
     * MailJet Status → Application EventType Mapping:
     * - "sent", "processed", "queued" → SENT
     * - "opened" → OPENED
     * - "clicked" → CLICKED
     * - "bounce", "softbounce", "hardbounce" → BOUNCED
     * - "spam" → SPAM
     * - "blocked" → BLOCKED
     * - "delivered" → DELIVERED
     * - "unsub" → UNSUBSCRIBED
     * 
     * MailJet MessageStatus Numeric Codes (for reference):
     * 0 = Processed
     * 1 = Queued
     * 2 = Sent
     * 3 = Opened
     * 4 = Clicked
     * 5 = Bounce
     * 6 = Spam
     * 7 = Unsub
     * 8 = Blocked
     * 9 = SoftBounce
     * 10 = HardBounce
     * 11 = Deferred
     * 
     * Case-Insensitive: Converts input to lowercase before matching
     * Unknown Status: Returns null, which is skipped during logging
     * 
     * @param status the message status string from MailJet API (case-insensitive)
     * @return the mapped MailjetEventType, or null if status is unknown/unmappable
     * 
     * @see MailjetEventType
     */
    private MailjetEventType mapStatusToEventType(String status) {
        return switch (status.toLowerCase()) {
            case "sent", "processed", "queued" -> MailjetEventType.SENT;
            case "opened" -> MailjetEventType.OPENED;
            case "clicked" -> MailjetEventType.CLICKED;
            case "bounce", "softbounce", "hardbounce" -> MailjetEventType.BOUNCED;
            case "spam" -> MailjetEventType.SPAM;
            case "blocked" -> MailjetEventType.BLOCKED;
            case "delivered" -> MailjetEventType.DELIVERED;
            case "unsub" -> MailjetEventType.UNSUBSCRIBED;
            default -> null;
        };
    }
}

