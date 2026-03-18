import type {
  EmailMaterial,
  FormMaterial,
  LandingPageMaterial,
  PostMaterial,
} from "../api/apiMaterials";

import type { EmailTemplate } from "../types/emailTemplate";
import type { FormTemplate } from "../types/formTemplate";
import type { LandingPage } from "../types/landingPage";
import type { Post } from "../types/post";
// EMAIL
export function materialToEmailTemplate(m: EmailMaterial): EmailTemplate {
  return {
    id: m.id,
    name: m.name,
    description: m.description ?? "",
    createdAt: m.createdAt,
    subject: m.subject ?? "",
    body: m.body ?? "",
    design: m.design ?? {},
  };
}

// FORM
export function materialToFormTemplate(material: FormMaterial): FormTemplate {
  return {
    id: material.id.toString(),
    name: material.name,
    description: material.description ?? "",
    schema: material.formJson ?? {},   // <--- map formJson → schema
    created_at: material.createdAt,
    created_by: "system",              // opcional, preencher o mínimo
  };
}


// LANDING PAGE
export function materialToLandingPage(m: LandingPageMaterial): LandingPage {
  return {
    id: m.id,
    name: m.name,
    description: m.description ?? "",
    body: m.body ?? "",
    design: "", // se tiveres design no backend adiciona aqui
    createdAt: m.createdAt,
  };
}


// SOCIAL POST
export function materialToPost(m: PostMaterial): Post {
  return {
    id: m.id,
    name: m.name,
    description: m.description ?? "",
    scheduled_date: m.scheduled_date ?? "",
    file_path: m.file_path ?? "",
    platforms: [], // obrigatório pelo tipo Post
  };
}