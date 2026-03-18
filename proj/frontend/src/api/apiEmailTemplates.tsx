import api from "./apiConfig";
import type { EmailTemplate } from "../types/emailTemplate";

export const getEmailTemplates = async (): Promise<EmailTemplate[]> => {
  const res = await api.get<EmailTemplate[]>("/email-templates");
  return res.data;
};

export const getEmailTemplateById = async (
  id: number
): Promise<EmailTemplate> => {
  const res = await api.get<EmailTemplate>(`/email-templates/${id}`);
  return res.data;
};

export const createEmailTemplate = async (
  template: EmailTemplate
): Promise<EmailTemplate> => {
  const res = await api.post<EmailTemplate>("/email-templates", template);
  return res.data;
};

export const updateEmailTemplate = async (
  id: number,
  updated: Partial<EmailTemplate>
): Promise<EmailTemplate> => {
  const res = await api.put<EmailTemplate>(`/email-templates/${id}`, updated);
  return res.data;
};

export const deleteEmailTemplate = async (id: number): Promise<void> => {
  await api.delete(`/email-templates/${id}`);
};

export const testEmailTemplate = async (id: number, testEmail: string): Promise<void> => {
  await api.post(`/email-templates/${id}/test`, { testEmail });
};
