import api from "./apiConfig";

export interface EmailLog {
  id: number;
  campaignId?: number;
  leadId?: number;
  emailAddress: string;
  mailjetMessageId: number;
  eventType: "SENT" | "OPENED" | "CLICKED" | "BOUNCED" | "SPAM" | "BLOCKED" | "DELIVERED" | "UNSUBSCRIBED";
  createdAt: string;
  subject?: string;
}

/**
 * Get all email logs for a specific lead
 */
export const getEmailLogsByLead = async (leadId: number): Promise<EmailLog[]> => {
  const res = await api.get<EmailLog[]>(`/email-logs/lead/${leadId}`);
  return res.data;
};

/**
 * Get all email logs for a specific campaign
 */
export const getEmailLogsByCampaign = async (campaignId: number): Promise<EmailLog[]> => {
  const res = await api.get<EmailLog[]>(`/email-logs/campaign/${campaignId}`);
  return res.data;
};

/**
 * Get all email logs (debugging)
 */
export const getAllEmailLogs = async (): Promise<EmailLog[]> => {
  const res = await api.get<EmailLog[]>(`/email-logs/all`);
  return res.data;
};

/**
 * Get all events for a specific MailJet message
 */
export const getEmailEventsByMessageId = async (messageId: number): Promise<EmailLog[]> => {
  const res = await api.get<EmailLog[]>(`/email-logs/message/${messageId}`);
  return res.data;
};
