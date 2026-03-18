package com.operimus.Marketing.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.operimus.Marketing.entities.Lead;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;



@Service
public class MailJetService {

  private static final Logger logger = LoggerFactory.getLogger(MailJetService.class);

  @Value("${mailjet.api-username}")
  private String apiUsername;

  @Value("${mailjet.api-password}")
  private String apiPassword;

  private final RestTemplate restTemplate = new RestTemplate();

  private final ObjectMapper objectMapper = new ObjectMapper();





  /**
   * Sends an email via MailJet and returns the MailJet message ID
   * @return the MailJet message ID if successful, or -1L if failed
   */
  public Long sendEmail(Lead lead, String sendFrom, String subject, String body) {
      System.out.println("Preparing to send email to: " + lead.getEmail());
        String url = "https://api.mailjet.com/v3.1/send";

        // Build the Variables map from Lead
        Map<String, Object> variables = new HashMap<>();
        variables.put("FirstName", lead.getFirstName() != null ? lead.getFirstName() : "");
        variables.put("LastName", lead.getLastName() != null ? lead.getLastName() : "");
        variables.put("Country", lead.getCountry() != null ? lead.getCountry() : "");

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("Email", lead.getEmail());
        recipient.put("Name", lead.getFirstName() != null ? lead.getFirstName() : lead.getEmail());
        recipient.put("Variables", variables); // always include
        
        System.out.println("Prepared recipient: " + recipient);
        System.out.println("Body: " + body);

        // Build message (avoid Map.of to allow empty strings when null)
        Map<String, Object> message = new HashMap<>();
        Map<String, Object> from = new HashMap<>();
        from.put("Email", sendFrom);
        from.put("Name", "Operimus");
        message.put("From", from);
        message.put("To", List.of(recipient));
        message.put("Subject", subject != null ? subject : "");
        String safeBody = body != null ? body : "";
        message.put("TextPart", safeBody);
        message.put("HTMLPart", "<html><body><p>" + safeBody + "</p></body></html>");

        Map<String, Object> payload = new HashMap<>();
        payload.put("Messages", List.of(message));

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
                        if (apiUsername == null || apiPassword == null || apiUsername.isEmpty() || apiPassword.isEmpty()) {
                                throw new IllegalStateException("Mailjet credentials are not configured. Please set MAILJET_API_USERNAME and MAILJET_API_PASSWORD.");
                        }
                        headers.setBasicAuth(apiUsername, apiPassword);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            if (response != null) {
                logger.debug("MailJet API Response: {}", response);
                Long messageId = extractMessageId(response);
                logger.info("Email sent successfully. MailJet Message ID: {}", messageId);
                return messageId;
            }
        } catch (Exception e) {
            logger.error("Error sending email via MailJet: {}", e.getMessage(), e);
        }

        logger.warn("Email send failed or returned invalid ID");
        return -1L;
  }

  /**
   * Extracts the MailJet message ID from the API response
   * MailJet v3.1/send returns a JSON response with a To array containing message objects with MessageID field
   * @param response the JSON response from MailJet API
   * @return the message ID if found, or -1L if extraction fails
   */
  private Long extractMessageId(String response) {
      try {
          JsonNode root = objectMapper.readTree(response);
          logger.debug("Parsing MailJet response: {}", response);
          
          // Correct structure: response.Messages[0].To[0].MessageID
          if (root.has("Messages") && root.get("Messages").isArray() && root.get("Messages").size() > 0) {
              JsonNode firstMessage = root.get("Messages").get(0);
              
              // Try Messages[0].To[0].MessageID
              if (firstMessage.has("To") && firstMessage.get("To").isArray() && firstMessage.get("To").size() > 0) {
                  JsonNode firstRecipient = firstMessage.get("To").get(0);
                  if (firstRecipient.has("MessageID")) {
                      Long messageId = firstRecipient.get("MessageID").asLong();
                      logger.info("Extracted MailJet Message ID: {}", messageId);
                      return messageId;
                  }
              }
              
              // Fallback to Messages[0].ID for older API versions
              if (firstMessage.has("ID")) {
                  Long messageId = firstMessage.get("ID").asLong();
                  logger.info("Extracted MailJet Message ID (legacy format): {}", messageId);
                  return messageId;
              }
          }
          
          // Also try root level To[0].MessageID (v3.1 format without Messages wrapper)
          if (root.has("To") && root.get("To").isArray() && root.get("To").size() > 0) {
              JsonNode firstRecipient = root.get("To").get(0);
              if (firstRecipient.has("MessageID")) {
                  Long messageId = firstRecipient.get("MessageID").asLong();
                  logger.info("Extracted MailJet Message ID (root To format): {}", messageId);
                  return messageId;
              }
          }
          
          logger.warn("MailJet response does not contain expected MessageID or ID field: {}", root);
      } catch (IOException e) {
          logger.error("Error parsing MailJet response: {}", e.getMessage(), e);
      }
      return -1L;
  }




}
