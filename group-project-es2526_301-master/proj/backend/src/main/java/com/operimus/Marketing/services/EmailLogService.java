package com.operimus.Marketing.services;

import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.entities.MailjetEventType;
import com.operimus.Marketing.repositories.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing email logs and MailJet event tracking.
 * 
 * This service handles:
 * - Logging email send events (SENT)
 * - Recording MailJet events (OPENED, CLICKED, BOUNCED, BLOCKED, etc.)
 * - Retrieving email logs by campaign, lead, or message ID
 * - Calculating email metrics (open rate, click rate, bounce rate)
 * 
 * Events are only saved if a valid leadId is available. This ensures data integrity
 * and prevents orphaned email log entries without associated leads.
 * 
 * @see EmailLog
 * @see MailjetEventType
 * @see MailJetPollingService
 */
@Service
public class EmailLogService {

    @Autowired
    private EmailLogRepository emailLogRepository;

    /**
     * Creates and saves an initial email log entry when an email is sent.
     * 
     * This method is called by the email sending service to record the SENT event.
     * The MailJet message ID returned from the API is stored for future event tracking.
     * 
     * Data Structure:
     * - campaignId: Links the email to a specific marketing campaign
     * - leadId: Links the email to the recipient (lead)
     * - mailjetMessageId: MailJet's unique identifier for this message
     * - eventType: Always SENT for this method
     * 
     * This SENT record serves as the parent for all subsequent events:
     * - When a BLOCKED/OPENED/CLICKED event arrives from MailJet
     * - The polling service uses mailjetMessageId to find this SENT record
     * - Lead/campaign info is copied to the event record
     * 
     * @param campaignId the campaign ID this email belongs to (required for tracking)
     * @param leadId the lead/recipient ID (required - must be valid)
     * @param emailAddress the recipient email address
     * @param mailjetMessageId the MailJet API response message ID (unique identifier)
     * @param subject the email subject line (for reference, currently unused)
     * 
     * @return the saved EmailLog entity with SENT event type and all metadata
     * 
     * @see #logEvent(Long, MailjetEventType)
     * @see MailJetPollingService
     */
    public EmailLog logEmail(Long campaignId, Long leadId, String emailAddress, Long mailjetMessageId, String subject) {
        EmailLog log = new EmailLog();
        log.setCampaignId(campaignId);
        log.setLeadId(leadId);
        log.setEmailAddress(emailAddress);
        log.setMailjetMessageId(mailjetMessageId);
        log.setEventType(MailjetEventType.SENT); // Initial email send event
        return emailLogRepository.save(log);
    }

    /**
     * Records a MailJet event (opened, clicked, bounced, blocked, etc.) for an email.
     * 
     * This method:
     * 1. Looks up the original SENT record using the mailjetMessageId
     * 2. Copies campaign ID, lead ID, and email address from the SENT record
     * 3. Only saves the event if a valid leadId is found
     * 
     * Event will NOT be saved if:
     * - No SENT record exists for the given mailjetMessageId
     * - The SENT record has no associated leadId
     * 
     * This ensures data integrity by preventing orphaned email log entries
     * that cannot be linked to a specific lead.
     * 
     * Data Flow:
     * - MailJet API (via polling service) returns message status
     * - MailJetPollingService calls this method to log the event
     * - EmailLog record is created/saved with lead and campaign context
     * 
     * Example Events:
     * - OPENED: User opened the email
     * - CLICKED: User clicked a link in the email
     * - BOUNCED: Email delivery failed (soft or hard bounce)
     * - BLOCKED: Email was blocked by recipient's mail server
     * - SPAM: Email marked as spam
     * - UNSUBSCRIBED: User unsubscribed
     * 
     * @param mailjetMessageId the unique MailJet message ID from API response
     * @param eventType the event type (OPENED, CLICKED, BOUNCED, BLOCKED, SPAM, etc.)
     * 
     * @return the saved EmailLog entry with event details, or null if:
     *         - No SENT record found for this messageId
     *         - SENT record has no leadId
     * 
     * @see MailjetEventType
     * @see EmailLog
     * @see MailJetPollingService
     */
    public EmailLog logEvent(Long mailjetMessageId, MailjetEventType eventType) {
        EmailLog event = new EmailLog();
        event.setMailjetMessageId(mailjetMessageId);
        event.setEventType(eventType);
        
        // Get the original email send record to copy campaign/lead info
        Optional<EmailLog> originalEmail = emailLogRepository.findByMailjetMessageIdAndEventType(
            mailjetMessageId, 
            MailjetEventType.SENT
        );
        
        if (originalEmail.isPresent()) {
            EmailLog original = originalEmail.get();
            event.setCampaignId(original.getCampaignId());
            event.setLeadId(original.getLeadId());
            event.setEmailAddress(original.getEmailAddress());
            
            // Only save if we have a leadId
            if (event.getLeadId() != null) {
                EmailLog saved = emailLogRepository.save(event);
                return saved;
            } else {
                // No leadId found - don't save the event
                return null;
            }
        } else {
            // No SENT record found - can't determine leadId, so don't save
            return null;
        }
    }

    /**
     * Retrieves all email logs (send + events) for a specific campaign.
     * 
     * @param campaignId the campaign ID to filter by
     * @return list of all email logs associated with the campaign (SENT, OPENED, CLICKED, etc.)
     */
    public List<EmailLog> getEmailLogsByCampaign(Long campaignId) {
        return emailLogRepository.findByCampaignId(campaignId);
    }

    /**
     * Retrieves all email logs (send + events) for a specific lead.
     * 
     * Returns all emails sent to this lead, including all engagement events.
     * 
     * @param leadId the lead ID to filter by
     * @return list of all email logs for this lead (ordered by creation date)
     */
    public List<EmailLog> getEmailLogsByLead(Long leadId) {
        return emailLogRepository.findByLeadId(leadId);
    }

    /**
     * Retrieves all email logs (send + events) for a specific campaign and lead.
     * 
     * Returns the communication history between a specific campaign and lead.
     * 
     * @param campaignId the campaign ID
     * @param leadId the lead ID
     * @return list of email logs for this campaign-lead pair
     */
    public List<EmailLog> getEmailLogsByCampaignAndLead(Long campaignId, Long leadId) {
        return emailLogRepository.findByCampaignIdAndLeadId(campaignId, leadId);
    }

    /**
     * Finds all events for a specific MailJet message.
     * 
     * Returns the complete event history for a single message, including:
     * - The original SENT event
     * - All subsequent events (OPENED, CLICKED, BOUNCED, BLOCKED, etc.)
     * 
     * @param mailjetMessageId the MailJet message ID
     * @return list of all events for this message, in chronological order
     */
    public List<EmailLog> getEmailEventsByMailjetMessageId(Long mailjetMessageId) {
        return emailLogRepository.findByMailjetMessageId(mailjetMessageId);
    }

    /**
     * Finds the original email send record for a MailJet message.
     * 
     * This is the parent record for all subsequent events. Used by:
     * - MailJet polling service to link events to leads
     * - Analytics to get original send metadata
     * 
     * @param mailjetMessageId the MailJet message ID
     * @return Optional containing the SENT record, empty if not found
     */
    public Optional<EmailLog> getEmailSendByMailjetMessageId(Long mailjetMessageId) {
        return emailLogRepository.findByMailjetMessageIdAndEventType(mailjetMessageId, MailjetEventType.SENT);
    }

    /**
     * Retrieves all email logs (send + events) from the entire table.
     * 
     * Warning: This can be a large dataset. Consider using filtered methods for large databases.
     * 
     * @return all EmailLog records in the database
     */
    public List<EmailLog> getAllEmailLogs() {
        return emailLogRepository.findAll();
    }

    /**
     * Gets all events of a specific type for a message (e.g., all OPENED events)
     */
    public List<EmailLog> getEventsByMailjetMessageAndType(Long mailjetMessageId, MailjetEventType eventType) {
        List<EmailLog> allEvents = emailLogRepository.findByMailjetMessageId(mailjetMessageId);
        return allEvents.stream()
            .filter(event -> event.getEventType() == eventType)
            .toList();
    }

    /**
     * Checks if an email was opened (has at least one OPENED event)
     */
    public boolean wasEmailOpened(Long mailjetMessageId) {
        return emailLogRepository.existsByMailjetMessageIdAndEventType(mailjetMessageId, MailjetEventType.OPENED);
    }

    /**
     * Checks if an email was already sent in a campaign to a lead
     */
    public boolean emailAlreadySent(Long campaignId, Long leadId) {
        return emailLogRepository.existsByCampaignIdAndLeadIdAndEventType(campaignId, leadId, MailjetEventType.SENT);
    }

    /**
     * Counts emails sent in a campaign
     */
    public long countEmailsInCampaign(Long campaignId) {
        return emailLogRepository.countByCampaignIdAndEventType(campaignId, MailjetEventType.SENT);
    }

    /**
     * Counts opened emails in a campaign
     */
    public long countOpenedEmailsInCampaign(Long campaignId) {
        return emailLogRepository.countByCampaignIdAndEventTypeIn(campaignId, Arrays.asList(MailjetEventType.OPENED));
    }

    /**
     * Counts clicked emails in a campaign
     */
    public long countClickedEmailsInCampaign(Long campaignId) {
        return emailLogRepository.countByCampaignIdAndEventTypeIn(campaignId, Arrays.asList(MailjetEventType.CLICKED));
    }

    /**
     * Counts bounced emails in a campaign
     */
    public long countBouncedEmailsInCampaign(Long campaignId) {
        return emailLogRepository.countByCampaignIdAndEventTypeIn(campaignId, Arrays.asList(MailjetEventType.BOUNCED));
    }

    /**
     * Counts emails sent to a specific lead
     */
    public long countEmailsToLead(Long leadId) {
        return emailLogRepository.countByLeadIdAndEventType(leadId, MailjetEventType.SENT);
    }

    /**
     * Counts opened emails for a lead
     */
    public long countOpenedEmailsToLead(Long leadId) {
        return emailLogRepository.countByLeadIdAndEventTypeIn(leadId, Arrays.asList(MailjetEventType.OPENED));
    }

    /**
     * Calculates email open rate for a campaign
     * @return percentage of opened emails (0-100)
     */
    public double getOpenRateForCampaign(Long campaignId) {
        long sent = countEmailsInCampaign(campaignId);
        if (sent == 0) return 0.0;
        long opened = countOpenedEmailsInCampaign(campaignId);
        return (opened * 100.0) / sent;
    }

    /**
     * Calculates click rate for a campaign
     * @return percentage of clicked emails (0-100)
     */
    public double getClickRateForCampaign(Long campaignId) {
        long sent = countEmailsInCampaign(campaignId);
        if (sent == 0) return 0.0;
        long clicked = countClickedEmailsInCampaign(campaignId);
        return (clicked * 100.0) / sent;
    }

    /**
     * Checks if an event has already been logged (deduplication check).
     * 
     * Uses a composite key: mailjetMessageId + eventType
     * Prevents duplicate events from being saved when MailJet sends the same status multiple times.
     * 
     * @param mailjetMessageId the MailJet message ID
     * @param eventType the event type to check
     * @return true if this exact event (messageId + type) already exists, false otherwise
     */
    public boolean eventExists(Long mailjetMessageId, MailjetEventType eventType) {
        return emailLogRepository.existsByMailjetMessageIdAndEventType(mailjetMessageId, eventType);
    }

    /**
     * Creates an email log event with full details (for debugging/testing)
     * @param mailjetMessageId the MailJet message ID
     * @param eventType the event type
     * @param leadId the lead ID (optional)
     * @param emailAddress the email address (optional)
     * @param campaignId the campaign ID (optional)
     * @return the saved EmailLog entry
     */
    public EmailLog logEventWithDetails(Long mailjetMessageId, MailjetEventType eventType, 
                                       Long leadId, String emailAddress, Long campaignId) {
        EmailLog event = new EmailLog();
        event.setMailjetMessageId(mailjetMessageId);
        event.setEventType(eventType);
        event.setLeadId(leadId);
        event.setEmailAddress(emailAddress);
        event.setCampaignId(campaignId);
        return emailLogRepository.save(event);
    }

    /**
     * Deletes all email logs for a specific campaign
     */
    public void deleteEmailLogsByCampaign(Long campaignId) {
        List<EmailLog> logs = emailLogRepository.findByCampaignId(campaignId);
        emailLogRepository.deleteAll(logs);
    }
}
