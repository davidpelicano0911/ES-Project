import api from "./apiConfig";
import type { Workflow, WorkflowInstance } from "../types/workflow";

export const getWorkflowByCampaignId = async (
  campaignId: number
): Promise<Workflow> => {
  const response = await api.get<Workflow>(`/campaigns/${campaignId}/workflow`);
  return response.data;
};

export const getWorkflowById = async (id: number): Promise<Workflow> => {
  const response = await api.get<Workflow>(`/workflows/${id}`);
  return response.data;
};

export const getWorkflows = async (): Promise<Workflow[]> => {
  const response = await api.get<Workflow[]>("/workflows");
  return response.data;
};

export const createWorkflow = async (payload: any): Promise<void> => {
  await api.post("/workflows", payload);
};

export const updateWorkflow = async (
  id: number,
  data: Partial<Workflow>
): Promise<Workflow> => {
  console.log("Updating workflow with data:", data);
  const response = await api.put(`/workflows/${id}`, data);
  return response.data;
};

export const deleteWorkflow = async (id: number): Promise<void> => {
  await api.delete(`/workflows/${id}`);
};

export const getCampaignNameById = async (id: number): Promise<string> => {
  const response = await api.get(`/campaigns/${id}`);
  return response.data.name;
};

export const createWorkflowFromTemplate = async (templateId: number, campaignId: number) => {
  const res = await api.post(`/workflows/campaigns/${campaignId}/from-template/${templateId}`);
  return res.data;
};

export const getWorkflowInstances = async (workflowId: number): Promise<WorkflowInstance[]> => {
  const response = await api.get<WorkflowInstance[]>(`/workflow-instances/workflow/${workflowId}`);
  return response.data;
};
