export type EmailEventType = "SENT" | "OPENED" | "CLICKED" | "BOUNCED" | "SPAM" | "BLOCKED" | "DELIVERED" | "UNSUBSCRIBED";

export interface EmailLog {
  id: number;
  campaignId?: number;
  leadId?: number;
  emailAddress: string;
  mailjetMessageId: number;
  eventType: EmailEventType;
  createdAt: string;
  subject?: string;
}
