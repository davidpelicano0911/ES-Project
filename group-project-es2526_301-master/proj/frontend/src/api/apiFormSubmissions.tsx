import api from "./apiConfig";

export interface FormSubmission {
  id: number;
  formId?: number;
  campaignId?: number;
  leadId: number;
  responsesJson: string;
  submittedAt: string;
  form?: {
    id: number;
    name: string;
  };
}

export const getFormSubmissionsByLeadId = async (leadId: number): Promise<FormSubmission[]> => {
  const resp = await api.get<FormSubmission[]>("/form-submissions", {
    params: { leadId },
  });
  return resp.data;
};

export const getFormSubmissionsByCampaignId = async (campaignId: number): Promise<FormSubmission[]> => {
  const resp = await api.get<FormSubmission[]>("/form-submissions", {
    params: { campaignId },
  });
  return resp.data;
};
