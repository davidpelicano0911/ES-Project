import api from "./apiConfig";
import type { Lead, LeadSyncStatusDTO } from "../types/lead";

export const getLeads = async (): Promise<Lead[]> => {
  const resp = await api.get<Lead[]>("/leads");
  return resp.data;
};

export const getLeadById = async (id: number): Promise<Lead> => {
  const resp = await api.get<Lead>(`/leads/${id}`);
  return resp.data;
};

export const getLeadSyncStatus = async (): Promise<LeadSyncStatusDTO> => {
  const resp = await api.get<LeadSyncStatusDTO>("/leads/sync-status");
  return resp.data;
}

export const syncLeads = async (): Promise<void> => {
  await api.put("/leads/sync-from-hubspot");
};
