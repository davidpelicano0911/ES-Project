package com.operimus.Marketing.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Unified email log and event tracking table
 * Records email sends and tracks all MailJet events (sent, opened, clicked, bounced, etc.)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_log", indexes = {
    @Index(name = "idx_campaign_id", columnList = "campaign_id"),
    @Index(name = "idx_lead_id", columnList = "lead_id"),
    @Index(name = "idx_mailjet_message_id", columnList = "mailjet_message_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_campaign_lead", columnList = "campaign_id,lead_id")
})
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Column(name = "lead_id")
    private Long leadId;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "mailjet_message_id", nullable = false)
    private Long mailjetMessageId;

    /**
     * Event type: SENT (initial send), or event from webhook (OPENED, CLICKED, BOUNCED, SPAM, BLOCKED, DELIVERED, UNSUBSCRIBED)
     * Initially set to SENT when email is sent. Updated to event type when webhook is received.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private MailjetEventType eventType = MailjetEventType.SENT;

    /**
     * Timestamp of the event (when email was sent, opened, clicked, bounced, etc.)
     */
    @Column(name = "event_timestamp")
    private Date eventTimestamp;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (eventTimestamp == null) {
            eventTimestamp = new Date();
        }
        if (eventType == null) {
            eventType = MailjetEventType.SENT;
        }
    }
}
