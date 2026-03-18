export interface FormTemplate {
  id: string;
  name: string;
  description: string;
  schema: any;

  views?: number;
  submissions?: number;
  conversion_rate?: number;
  abandonment_rate?: number;
  field_completion_rate?: number;

  created_by?: string;
  created_at?: string;
}
