import api from "./apiConfig";
import { type DashboardPayload } from "../types/dashboard";

export const createDashboard = async (
  campaignId: number,
  payload: { title: string }
): Promise<void> => {
  await api.post(`/campaigns/${campaignId}/dashboard`, payload);
};

export const updateDashboardLayout = async (
  campaignId: number,
  payload: DashboardPayload
): Promise<any> => {
  const response = await api.put(`/campaigns/${campaignId}/dashboard`, payload);
  return response.data;
};

export const getCampaignDashboard = async (campaignId: number): Promise<any> => {
  const response = await api.get(`/campaigns/${campaignId}/dashboard`);
  return response.data;
};

export const getFormPerformanceStats = async (): Promise<any[]> => {
  const response = await api.get('/form-submissions/stats');
  return response.data;
};