import api from "./apiConfig";
import type { LandingPageEvent } from "../types/landingPageEvent";

export const getLandingPageEventsByLeadId = async (
  leadId: number
): Promise<LandingPageEvent[]> => {
  const res = await api.get(`/landing-page-events`, { params: { leadId } });
  return res.data;
};
