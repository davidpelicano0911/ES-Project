import type { Workflow } from "./workflow";
import type { Dashboard } from "./dashboard";
import type { Segment } from "./segment";

export interface Campaign {
  id: number;
  name: string;
  description: string;
  segment: Segment[];
  createdAt: string;
  dueDate: string;
  status: "ACTIVE" | "IN_PROGRESS" | "FINISHED";
  workflow?: Workflow;
  dashboard?: Dashboard;
}
