export interface Dashboard {
  id: number;
  title: string;
  layoutData?: string;
}

export interface DashboardPayload {
  title?: string;
  layoutData: string;
}