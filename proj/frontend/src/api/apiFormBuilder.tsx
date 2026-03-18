import api from './apiConfig';

export interface FormComponent {
  type: string;
  key: string;
  label: string;
  input: boolean;
  tableView?: boolean;
  validateWhenHidden?: boolean;
  applyMaskOn?: string;
  disableOnInvalid?: boolean;
  action?: string;
  additionalProperties?: any;
}

export interface FormTemplate {
    name: string;
    description: string | null;
    createdBy?: number;
    createdAt?: string;
    updatedAt?: string;
    isPublished?: boolean;
    formJson?: string; 
}

export const createTemplate = async (template: FormTemplate): Promise<FormTemplate> => {
  const res = await api.post<FormTemplate>('/form-template', template);
  return res.data;
};

export const getAllTemplates = async (): Promise<FormTemplate[]> => {
  const res = await api.get<FormTemplate[]>('/form-template');
  return res.data;
};

export const getTemplate = async (id: string): Promise<FormTemplate> => {
  const res = await api.get<FormTemplate>(`/form-template/${id}`);
  return res.data;
};

export const deleteTemplate = async (id: string): Promise<FormTemplate> => {
  const res = await api.delete<FormTemplate>(`/form-template/${id}`);
  return res.data;
};

export const updateTemplate = async (id: string, payload: FormTemplate): Promise<FormTemplate> => {
  const res = await api.put<FormTemplate>(`/form-template/${id}`, payload);
  return res.data;
};

export default {
  createTemplate,
  getAllTemplates,
  getTemplate,
  deleteTemplate,
  updateTemplate,
};
