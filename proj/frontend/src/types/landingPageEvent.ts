export type LandingPageEvent = {
  id: number;
  landingPageId?: number | null;
  leadId?: number | null;
  eventType: string;
  metadataJson?: string | null;
  createdAt: string; // ISO timestamp
};
