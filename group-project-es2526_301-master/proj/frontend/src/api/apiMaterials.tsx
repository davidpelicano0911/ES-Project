import api from "./apiConfig";

export interface BaseMaterial {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
  modifiedAt?: string;
  type: string;  // <- campo discriminador
}

// EMAIL
export interface EmailMaterial extends BaseMaterial {
  type: "EMAIL";
  subject: string;
  body: string;
  design?: any;
}

// FORM
export interface FormMaterial extends BaseMaterial {
  type: "FORM";
  formJson: any;
}

// LANDING PAGE
export interface LandingPageMaterial extends BaseMaterial {
  type: "LANDING_PAGE";
  body: string;
}

// POST
export interface PostMaterial extends BaseMaterial {
  type: "POST";
  file_path: string;
  scheduled_date?: string;
}

export type Material =
  | EmailMaterial
  | FormMaterial
  | LandingPageMaterial
  | PostMaterial;

// API CALLS
export async function getMaterialsByCampaign(campaignId: number): Promise<Material[]> {
  const res = await api.get(`/campaigns/${campaignId}/materials`);
  return res.data;
}

export async function detachMaterial(campaignId: number, materialId: number) {
  await api.delete(`/campaigns/${campaignId}/materials/${materialId}`);
}

export async function attachMaterial(campaignId: number, materialId: number) {
    await api.post(`/campaigns/${campaignId}/materials/${materialId}`);
}

export async function getAllMaterials(): Promise<Material[]> {
  const res = await api.get("/campaigns/materials");
  return res.data;
}


