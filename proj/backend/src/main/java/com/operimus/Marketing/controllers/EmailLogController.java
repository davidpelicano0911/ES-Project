package com.operimus.Marketing.controllers;

import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.services.EmailLogService;
import com.operimus.Marketing.services.MailJetPollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST API controller for querying email logs
 * Provides endpoints to retrieve email send and event data
 */
@RestController
@RequestMapping("/api/${api.version}/email-logs")
public class EmailLogController {

    @Autowired
    private EmailLogService emailLogService;

    @Autowired
    private MailJetPollingService mailJetPollingService;

    /**
     * Get all email logs from the entire table
     * @return list of all email sends and events
     */
    @GetMapping("/all")
    public ResponseEntity<List<EmailLog>> getAllEmailLogs() {
        List<EmailLog> logs = emailLogService.getAllEmailLogs();
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all email logs for a specific campaign
     * @param campaignId the campaign ID
     * @return list of all email sends and events for this campaign
     */
    @GetMapping("/campaign/{campaignId}")
    public ResponseEntity<List<EmailLog>> getEmailLogsByCampaign(@PathVariable Long campaignId) {
        List<EmailLog> logs = emailLogService.getEmailLogsByCampaign(campaignId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all email logs for a specific lead
     * @param leadId the lead ID
     * @return list of all email sends and events for this lead
     */
    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<EmailLog>> getEmailLogsByLead(@PathVariable Long leadId) {
        List<EmailLog> logs = emailLogService.getEmailLogsByLead(leadId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all email logs for a specific campaign and lead
     * @param campaignId the campaign ID
     * @param leadId the lead ID
     * @return list of all emails sent to this lead in this campaign
     */
    @GetMapping("/campaign/{campaignId}/lead/{leadId}")
    public ResponseEntity<List<EmailLog>> getEmailLogsByCampaignAndLead(
            @PathVariable Long campaignId,
            @PathVariable Long leadId) {
        List<EmailLog> logs = emailLogService.getEmailLogsByCampaignAndLead(campaignId, leadId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all events (send + subsequent events) for a specific MailJet message
     * @param messageId the MailJet message ID
     * @return list of all events for this message
     */
    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<EmailLog>> getEmailEventsByMessageId(@PathVariable Long messageId) {
        List<EmailLog> events = emailLogService.getEmailEventsByMailjetMessageId(messageId);
        return ResponseEntity.ok(events);
    }
}
