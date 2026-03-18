import api from "./apiConfig";
import type { Campaign } from "../types/campaign";

export const getCampaigns = async (): Promise<Campaign[]> => {
  const response = await api.get<Campaign[]>("/campaigns/");
  return response.data;
};

export const getCampaignById = async (id: number): Promise<Campaign> => {
  const response = await api.get<Campaign>(`/campaigns/${id}`);
  return response.data;
};

export const createCampaign = async (payload: any): Promise<void> => {
  await api.post("/campaigns/", payload);
};

export const updateCampaign = async (
  id: number,
  data: Partial<Campaign>
): Promise<void> => {
  await api.put(`/campaigns/${id}`, data);
};

export const deleteCampaign = async (id: number): Promise<void> => {
  await api.delete(`/campaigns/${id}`);
};

export const getCampaignMaterials = async (campaignId: number): Promise<any[]> => {
  const response = await api.get<any[]>(`/campaigns/${campaignId}/materials`);
  return response.data;
}

export const getCampaignReports = async (id: number) => {
  const response = await api.get(`/campaigns/${id}/reports`);
  return response.data;
};

 export const uploadCampaignReport = async (campaignId: number, file: Blob, fileName: string) => {
  const formData = new FormData();
  formData.append("file", file, fileName);
  formData.append("name", fileName)

  const response = await api.post(`/campaigns/${campaignId}/reports`, formData);
  return response.data;
};


export const downloadCampaignReport = async (reportId: number, fileName: string) => {
  const response = await api.get(`/campaigns/reports/${reportId}/download`, {
    responseType: "blob",
  });

  const blob = new Blob([response.data], { type: "application/pdf" });
  const url = window.URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = fileName;
  link.click();

  window.URL.revokeObjectURL(url);
};

