import api from "./apiConfig"; 
import type { LandingPage } from "../types/landingPage";

export const getLandingPages = async (): Promise<LandingPage[]> => {
  const res = await api.get("/landing-pages");
  return res.data;
};

export const getLandingPageById = async (
  id: number
): Promise<LandingPage> => {
  const res = await api.get(`/landing-pages/${id}`);
  return res.data;
};

export const createLandingPage = async (
  page: Omit<LandingPage, "id" | "createdAt">
): Promise<LandingPage> => {
  const res = await api.post("/landing-pages", page);
  return res.data;
};

export const updateLandingPage = async (
  id: number,
  updated: Partial<LandingPage>
): Promise<LandingPage> => {
  const res = await api.put(`/landing-pages/${id}`, updated);
  return res.data;
};

export const deleteLandingPage = async (id: number): Promise<void> => {
  await api.delete(`/landing-pages/${id}`);
};

