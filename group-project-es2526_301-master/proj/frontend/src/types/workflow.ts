export interface Workflow {
  id: number;
  name: string;
  description: string;
  createdAt: string;
  modifiedAt?: string;
  nodes: WorkflowNode[];
  isReadyToUse: boolean;
}

export type NodeType =
  | "START"
  | "EMAIL"
  | "DELAY"
  | "LANDING_PAGE"
  | "FORM"
  | "END"
  | "ADD_TO_SEGMENT"
  | "REMOVE_FROM_SEGMENT"
;

export type EventType =
  | "FORM_SUBMITTED"
  | "SEND_EMAIL"
  | "ADD_TO_SEGMENT"
  | "REMOVE_FROM_SEGMENT"
  | "DELAY"
;


export interface WorkflowNode {
  id: number;
  positionX: number;
  positionY: number;
  isStartNode: boolean;
  isEndNode: boolean;
  score: number;
  nodeType: NodeType;
  eventType?: EventType;
  outgoingNodes: (WorkflowNode | { id: number })[];
  label?: string;
}

// Simple child interfaces that extend the base
export interface EmailNode extends WorkflowNode {
  nodeType: "EMAIL";
  emailTemplateId?: number;
  sendFrom?: string;
}

export interface FormNode extends WorkflowNode {
  nodeType: "FORM";
  formId?: number;
}

export interface DelayNode extends WorkflowNode {
  nodeType: "DELAY";
  delayDuration?: number;
  delayUnit?: string;
}

export interface SegmentNode extends WorkflowNode {
  nodeType: "ADD_TO_SEGMENT" | "REMOVE_FROM_SEGMENT";
  segmentId?: number;
}

export interface StartNode extends WorkflowNode {
  nodeType: "START";
}

export interface EndNode extends WorkflowNode {
  nodeType: "END";
}

export interface LandingPageNode extends WorkflowNode {
  nodeType: "LANDING_PAGE";
}

// Union type of all node types
export type AnyWorkflowNode = 
  | EmailNode 
  | FormNode 
  | DelayNode
  | SegmentNode
  | StartNode
  | EndNode
  | LandingPageNode;

export interface WorkflowEdge {
  id: number;
  fromNode: WorkflowNode;
  toNode: WorkflowNode;
}

export interface WorkflowTemplate {
  id: number;
  name: string;
  description: string;
  templateData?: string;
  createdAt: string;
}

export interface WorkflowInstance {
  id: number;
  finished: boolean;
  lead?: {
    id: number;
  };
}
