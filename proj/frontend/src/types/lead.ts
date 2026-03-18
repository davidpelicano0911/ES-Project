export type LeadStatus = "Hot" | "Warm" | "Cold";

export interface Lead {
  id: number;
  firstName: string;
  lastName: string;
  country?: string;
  email: string;
  phoneNumber?: string;
  createdAt?: string;
  isSubscribed?: boolean;
  segmentIds: number[];
  score?: number;
  status?: LeadStatus;
}

export interface LeadSyncStatusDTO {
  updatesAvailable: boolean;
  lastSyncedAt: string | null;
  pendingUpdates: number;
  pendingCreates: number;
}