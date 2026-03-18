package com.operimus.Marketing.entities;

/**
 * MailJet event types that can be tracked via webhooks
 */
public enum MailjetEventType {
    SENT,           // Email successfully sent
    OPENED,         // Email was opened by recipient
    CLICKED,        // Link in email was clicked
    BOUNCED,        // Email bounced (hard or soft)
    SPAM,           // Email marked as spam
    BLOCKED,        // Email blocked by MailJet
    DELIVERED,      // Email delivered to recipient's mail server
    UNSUBSCRIBED    // Recipient unsubscribed
}
