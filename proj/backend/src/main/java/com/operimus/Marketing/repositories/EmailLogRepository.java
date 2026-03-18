package com.operimus.Marketing.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.operimus.Marketing.entities.EmailLog;
import com.operimus.Marketing.entities.MailjetEventType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    
    /**
     * Find all email logs for a specific campaign (including all events)
     */
    List<EmailLog> findByCampaignId(Long campaignId);

    /**
     * Find all email logs for a specific lead (including all events)
     */
    List<EmailLog> findByLeadId(Long leadId);

    /**
     * Find all email logs for a specific campaign and lead
     */
    List<EmailLog> findByCampaignIdAndLeadId(Long campaignId, Long leadId);

    /**
     * Find email log by MailJet message ID with SENT event (initial send record)
     */
    Optional<EmailLog> findByMailjetMessageIdAndEventType(Long mailjetMessageId, MailjetEventType eventType);

    /**
     * Find all events for a specific MailJet message ID
     */
    List<EmailLog> findByMailjetMessageId(Long mailjetMessageId);

    /**
     * Find all events of a specific type for a campaign
     */
    List<EmailLog> findByCampaignIdAndEventType(Long campaignId, MailjetEventType eventType);

    /**
     * Check if an email was opened (has OPENED event)
     */
    boolean existsByMailjetMessageIdAndEventType(Long mailjetMessageId, MailjetEventType eventType);

    /**
     * Check if email was already sent to a lead in a campaign
     */
    boolean existsByCampaignIdAndLeadIdAndEventType(Long campaignId, Long leadId, MailjetEventType eventType);

    /**
     * Count emails sent in a campaign (count SENT events)
     */
    long countByCampaignIdAndEventType(Long campaignId, MailjetEventType eventType);

    /**
     * Count opened emails in a campaign
     */
    long countByCampaignIdAndEventTypeIn(Long campaignId, List<MailjetEventType> eventTypes);

    /**
     * Count emails sent to a lead (count SENT events)
     */
    long countByLeadIdAndEventType(Long leadId, MailjetEventType eventType);

    /**
     * Count specific events for a lead
     */
    long countByLeadIdAndEventTypeIn(Long leadId, List<MailjetEventType> eventTypes);

    /**
     * Count all events of a specific type for a message
     */
    long countByMailjetMessageIdAndEventType(Long mailjetMessageId, MailjetEventType eventType);
}
