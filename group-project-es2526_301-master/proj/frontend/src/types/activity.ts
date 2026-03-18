export type Activity = {
  id: string;
  // extended to include additional event kinds used in the UI
  type: "timeline" | "email" | "form" | "landing" | "note" | "talk" | "link" | "website";
  title: string;
  description?: string;
  timestamp: string; // ISO
};
