import api from "./apiConfig";
import { type WorkflowTemplate } from "../types/workflow";

export async function getWorkflowTemplates(): Promise<WorkflowTemplate[]> {
  const response = await api.get('/workflow-templates');
  return response.data;
}

export async function getWorkflowTemplateById(id: number): Promise<WorkflowTemplate> {
  const response = await api.get(`/workflow-templates/${id}`);
  console.log("API RESPONSE:", response.data);
  return response.data;
}

export const createWorkflowTemplate = async (data: any): Promise<void> => {
  await api.post('/workflow-templates', data);
}

export async function updateWorkflowTemplate(id: number, data: Partial<WorkflowTemplate>): Promise<WorkflowTemplate> {
  const response = await api.put(`/workflow-templates/${id}`, data);
  console.log("API RESPONSE:", response.data);
  return response.data;
}

export async function deleteWorkflowTemplate(id: number): Promise<void> {
  await api.delete(`/workflow-templates/${id}`);
}
