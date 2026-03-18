import { useState, useEffect } from 'react';
import * as api from '../api/apiFormBuilder';
import { type FormTemplate } from '../api/apiFormBuilder';

export const useFormTemplates = () => {
  const [templates, setTemplates] = useState<FormTemplate[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTemplates = async () => {
    try {
      setLoading(true);
      setError(null);
  const data = await api.getAllTemplates();
      setTemplates(data);
    } catch (err) {
      setError('Failed to fetch form templates');
      console.error('Error fetching templates:', err);
    } finally {
      setLoading(false);
    }
  };

  const createTemplate = async (template: FormTemplate) => {
    try {
      setError(null);
  const newTemplate = await api.createTemplate(template);
      // Refresh the templates list
      await fetchTemplates();
      return newTemplate;
    } catch (err) {
      setError('Failed to create form template');
      console.error('Error creating template:', err);
      throw err;
    }
  };



  const getTemplate = async (id: string): Promise<FormTemplate> => {
    try {
      setError(null);
  return await api.getTemplate(id);
    } catch (err) {
      setError('Failed to get form template');
      console.error('Error getting template:', err);
      throw err;
    }
  };

  const deleteTemplate = async (id: string): Promise<FormTemplate> => {
    try {
      setError(null);
  const resp = await api.deleteTemplate(id);
      // Refresh the templates list
      await fetchTemplates();
      return resp;
    } catch (err) {
      setError('Failed to delete form template');
      console.error('Error deleting template:', err);
      throw err;
    }
  };

  useEffect(() => {
    fetchTemplates();
  }, []);

  return {
    templates,
    loading,
    error,
    fetchTemplates,
    createTemplate,
    deleteTemplate,
    getTemplate,
  };
};
