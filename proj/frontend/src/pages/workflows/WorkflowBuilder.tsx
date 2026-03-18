import { useCallback, useEffect, useState } from "react";
import {
  ReactFlow,
  addEdge,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  type Node as RFNode,
  type Edge as RFEdge,
  type Connection,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useParams } from "react-router-dom";
import { Save } from "lucide-react";
import SidebarProperties from "../../components/sidebars/SidebarProperties";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import WarningMessage from "../../components/messages/WarningMessage";
import WorkflowBlocksBar from "../../components/WorkflowBlocksBar";
import BackButton from "../../components/buttons/BackButton";
import {
  getWorkflowById,
  updateWorkflow,
  getCampaignNameById,
  getWorkflowByCampaignId,
} from "../../api/apiWorkflows";
import type {
  Workflow,
  WorkflowNode,
  NodeType,
  EventType,
  AnyWorkflowNode,
  EmailNode,
  FormNode,
  DelayNode,
  SegmentNode,
  StartNode,
  EndNode,
  LandingPageNode,
} from "../../types/workflow";
import { useUser } from "../../context/UserContext";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const WorkflowBuilder = () => {
  const { id } = useParams();
  const [campaignName, setCampaignName] = useState<string>("");
  const [nodes, setNodes, onNodesChange] = useNodesState<RFNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<RFEdge>([]);
  const [selectedNode, setSelectedNode] = useState<RFNode | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showFail, setShowFail] = useState(false);
  const [showWarning, setShowWarning] = useState(false);
  const [workflowId, setWorkflowId] = useState<number | null>(null);
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.WorkflowBuilder.Load", (span) => {
      span.setAttribute("page.name", "WorkflowBuilder");
      span.setAttribute("user.hasRole.MARKETING_MANAGER", hasRole("MARKETING_MANAGER"));
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer, hasRole]);

  const getNodeAppearance = (
    nodeType: string,
    isStartNode = false,
    isEndNode = false
  ) => {
    if (isStartNode) return { label: "Start", color: "#22C55E" };
    if (isEndNode) return { label: "End", color: "#6B7280" };
    switch (nodeType?.toUpperCase()) {
      case "EMAIL":
        return { label: "Send Email", color: "#2563EB" };
      case "DELAY":
        return { label: "Delay", color: "#F59E0B" };
      case "FORM":
        return { label: "Form Submission", color: "#06B6D4" };
      case "LANDING_PAGE":
        return { label: "Landing Page", color: "#EC4899" };
      case "ADD_TO_SEGMENT":
        return { label: "Add to Segment", color: "#16A34A" };
      case "REMOVE_FROM_SEGMENT":
        return { label: "Remove from Segment", color: "#DC2626" };
      default:
        return { label: nodeType || "Node", color: "#2563EB" };
    }
  };

  useEffect(() => {
    const fetchCampaign = async () => {
      tracer.startActiveSpan("API.GetCampaignNameById", async (span) => {
        if (!id) {
          span.setStatus({ code: SpanStatusCode.ERROR, message: "No campaign ID provided" });
          span.end();
          return;
        }
        try {
          const name = await getCampaignNameById(Number(id));
          setCampaignName(name);
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          span.addEvent("campaign_name_fetched_successfully");
          span.setAttribute("campaign.name", name);
          span.end();
        } catch (err) {
          console.error("Error fetching campaign name:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          span.addEvent("campaign_name_fetch_failed");
          span.end();
        }
      });
    };
    fetchCampaign();
  }, [id]);

  useEffect(() => {
    const fetchWorkflow = async () => {
      tracer.startActiveSpan("API.GetWorkflowByCampaignId", async (span) => {
        if (!id) {
          span.setStatus({ code: SpanStatusCode.ERROR, message: "No campaign ID provided" });
          span.end();
          return;
        }

        try {
          const workflow: Workflow = await getWorkflowByCampaignId(Number(id));
          setWorkflowId(workflow.id);
          span.addEvent("workflow_fetched_successfully");
          span.setAttribute("workflow.id", workflow.id);
          span.setAttribute("api.success", true);

          if (!workflow.nodes || workflow.nodes.length === 0) {
            const startNode: RFNode = {
              id: `${Date.now()}-start`,
              type: "input",
              data: { label: "Start", color: "#22C55E", isStartNode: true },
              position: { x: 150, y: 200 },
              style: {
                backgroundColor: "#22C55E",
                color: "#fff",
                padding: "12px 16px",
                borderRadius: 10,
                fontWeight: 500,
                textAlign: "center",
                boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
              },
            };
            setNodes([startNode]);
            setEdges([]);
            span.addEvent("workflow_empty_initialized_with_start_node");
            span.setAttribute("workflow.nodes.count", 0);
            span.setStatus({ code: SpanStatusCode.OK });
            span.end();
            return;
          }

          const formattedNodes: RFNode[] = workflow.nodes.map((n) => {
            const { label: defaultLabel, color } = getNodeAppearance(
              n.nodeType,
              n.isStartNode,
              n.isEndNode
            );
            return {
              id: n.id.toString(),
              position: { x: n.positionX, y: n.positionY },
              data: {
                label: (n as any).label || defaultLabel,
                color,
                isStartNode: n.isStartNode,
                isEndNode: n.isEndNode,
                nodeType: n.nodeType,
                eventType: n.eventType,
                formId: (n as any).formId,
                emailTemplateId: (n as any).emailTemplateId,
                segmentId: (n as any).segmentId,
                delayDuration: (n as any).delayDuration,
                delayUnit: (n as any).delayUnit,
                sendFrom: (n as any).sendFrom,
              },
              style: {
                backgroundColor: color,
                color: "#fff",
                padding: "12px 16px",
                borderRadius: 10,
                fontWeight: 500,
                textAlign: "center",
                boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
              },
            };
          });

          const formattedEdges: RFEdge[] = workflow.nodes.flatMap((n) =>
            (n.outgoingNodes || []).map((target) => ({
              id: `edge-${n.id}-${target.id}`,
              source: n.id.toString(),
              target: target.id.toString(),
            }))
          );

          setNodes(formattedNodes);
          setEdges(formattedEdges);
          span.setAttribute("workflow.nodes.count", workflow.nodes.length);
          span.setStatus({ code: SpanStatusCode.OK });
          span.end();
        } catch (err) {
          console.error("Error fetching workflow:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          span.addEvent("workflow_fetch_failed");
          span.end();
        }
      });
    };

    fetchWorkflow();
  }, [id, setNodes, setEdges]);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const onNodeClick = useCallback((_: any, node: RFNode) => {
    tracer.startActiveSpan("User.ACTION.ClickNodeInWorkflowBuilder", (span) => {
      span.addEvent("node_clicked");
      span.setAttribute("node.id", node.id);
      setSelectedNode(node);
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  }, []);

  const handleAddNode = useCallback(
    (label: string, color: string, nodeType: string, eventType?: string) => {
      tracer.startActiveSpan("User.ACTION.AddNodeInWorkflowBuilder", (span) => {
        try {
          const newId = `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
          const isEndNode = label === "End";
          
          // Initialize default data based on node type
          let nodeData: any = { label, color, nodeType, isEndNode, eventType };
          
          // Add type-specific default values
          if (nodeType === "DELAY") {
            nodeData.delayDuration = 1;
            nodeData.delayUnit = "HOURS";
          }
          
          if (nodeType === "EMAIL") {
            nodeData.sendFrom = "marketingoperimus@gmail.com";
          }
          
          const newNode: RFNode = {
            id: newId,
            data: nodeData,
            position: { x: 200 * nodes.length, y: 300 },
            style: {
              backgroundColor: color,
              color: "#fff",
              padding: "12px 16px",
              borderRadius: 10,
              fontWeight: 500,
              textAlign: "center",
              boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
            },
          };
          setNodes((nds) => [...nds, newNode]);
          span.addEvent("node_added");
          span.setAttribute("node.id", newId);
          span.setStatus({ code: SpanStatusCode.OK });
        } catch (err) {
          span.recordException(err as Error);
          span.addEvent("node_add_failed");
          span.setAttribute("error.message", String(err));
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        } finally {
          span.end();
        }
      });
    },
    [nodes.length, setNodes, tracer]
  );

  const handleUpdateNode = useCallback(
    (field: string, value: any) => {
      tracer.startActiveSpan("User.ACTION.UpdateNodeInWorkflowBuilder", (span) => {
        if (!selectedNode) {
          span.setStatus({ code: SpanStatusCode.ERROR, message: "No node selected" });
          span.end();
          return;
        }

        setNodes((nds) =>
          nds.map((node) => {
            if (node.id !== selectedNode.id) return node;

            const updatedData = { ...node.data, [field]: value };
            
            // Special handling for label updates to ensure visual refresh
            if (field === "label") {
              updatedData.label = value;
            }
            
            // Type-specific property handling
            const nodeType = node.data?.nodeType as NodeType;
            switch (nodeType) {
              case "EMAIL":
                if (field === "template" || field === "emailTemplateId") {
                  updatedData.emailTemplateId = typeof value === 'string' ? (value ? parseInt(value, 10) : undefined) : value;
                }
                break;
              case "FORM":
                if (field === "form" || field === "formId") {
                  updatedData.formId = value;
                }
                break;
              case "DELAY":
                if (field === "delayDuration") {
                  updatedData.delayDuration = Number(value);
                }
                if (field === "delayUnit") {
                  updatedData.delayUnit = value;
                }
                break;
              case "ADD_TO_SEGMENT":
              case "REMOVE_FROM_SEGMENT":
                if (field === "segment" || field === "segmentId") {
                  updatedData.segmentId = typeof value === 'string' ? (value ? parseInt(value, 10) : undefined) : value;
                }
                break;
            }

            const updatedNode = { ...node, data: updatedData };
            
            // Update selected node if it's the one being modified
            if (node.id === selectedNode.id) {
              setSelectedNode(updatedNode);
            }

            return updatedNode;
          })
        );

        span.addEvent("node_updated");
        span.setAttribute("node.id", selectedNode.id);
        span.setAttribute("updated.field", field);
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      });
    },
    [selectedNode, setNodes, tracer]
  );

  const handleNodesDelete = useCallback(
    (deleted: RFNode[]) => {
      tracer.startActiveSpan("User.ACTION.DeleteNodeInWorkflowBuilder", (span) => {
        setNodes((nds) => nds.filter((n) => !deleted.some((d) => d.id === n.id)));
        setEdges((eds) =>
          eds.filter(
            (e) => !deleted.some((d) => d.id === e.source || d.id === e.target)
          )
        );
        if (selectedNode && deleted.some((d) => d.id === selectedNode.id)) {
          setSelectedNode(null);
        }
        span.addEvent("nodes_deleted");
        span.setAttribute("deleted.nodes.count", deleted.length);
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      });
    },
    [selectedNode, setNodes, setEdges]
  );

  // Type-safe node creation helper
  const createTypedWorkflowNode = (rfNode: RFNode): AnyWorkflowNode => {
    const baseNode: WorkflowNode = {
      id: /^\d+$/.test(rfNode.id) ? Number(rfNode.id) : 0,
      positionX: Math.round(rfNode.position.x),
      positionY: Math.round(rfNode.position.y),
      isStartNode: Boolean(rfNode.data?.isStartNode),
      isEndNode: Boolean(rfNode.data?.isEndNode),
      score: 0,
      nodeType: (rfNode.data?.nodeType as NodeType) || "START",
      eventType: rfNode.data?.eventType as EventType,
      outgoingNodes: [],
      label: rfNode.data?.label as string | undefined
    };

    // Create typed nodes based on nodeType
    switch (baseNode.nodeType) {
      case "EMAIL":
        return {
          ...baseNode,
          nodeType: "EMAIL",
          emailTemplateId: rfNode.data?.emailTemplateId ? (typeof rfNode.data.emailTemplateId === 'string' ? parseInt(rfNode.data.emailTemplateId, 10) : rfNode.data.emailTemplateId) : undefined,
          sendFrom: rfNode.data?.sendFrom as string | undefined,
        } as EmailNode;
      
    case "FORM":
      return {
        ...baseNode,
        nodeType: "FORM",
        formId: rfNode.data?.formId ? (typeof rfNode.data.formId === 'string' ? parseInt(rfNode.data.formId, 10) : rfNode.data.formId) : undefined,
      } as FormNode;      case "DELAY":
        return {
          ...baseNode,
          nodeType: "DELAY",
          delayDuration: rfNode.data?.delayDuration,
          delayUnit: rfNode.data?.delayUnit,
        } as DelayNode;
      
      case "ADD_TO_SEGMENT":
      case "REMOVE_FROM_SEGMENT":
        return {
          ...baseNode,
          nodeType: baseNode.nodeType,
          segmentId: rfNode.data?.segmentId ? (typeof rfNode.data.segmentId === 'string' ? parseInt(rfNode.data.segmentId, 10) : rfNode.data.segmentId) : undefined,
        } as SegmentNode;
      
      case "START":
        return { ...baseNode, nodeType: "START" } as StartNode;
      
      case "END":
        return { ...baseNode, nodeType: "END" } as EndNode;
      
      case "LANDING_PAGE":
        return { ...baseNode, nodeType: "LANDING_PAGE" } as LandingPageNode;
      
      default:
        return { ...baseNode, nodeType: "START" } as StartNode;
    }
  };

  const handleSave = async () => {
    tracer.startActiveSpan("API.UpdateWorkflow", async (span) => {
      if (!id) {
        span.setStatus({ code: SpanStatusCode.ERROR, message: "No campaign ID provided" });
        span.end();
        return;
      }

      try {
        const formattedNodes: AnyWorkflowNode[] = nodes.map(createTypedWorkflowNode);

        console.log("Formatted nodes for saving:", formattedNodes);

        if (!workflowId) throw new Error("Workflow ID not found");
        await updateWorkflow(workflowId, {
          name: campaignName || `Workflow-${id}`,
          description: "Auto-saved from Workflow Builder",
          nodes: formattedNodes,
        });

        const refreshedWorkflow = await getWorkflowById(workflowId);

        const nodeMap = new Map<string, number>();

        refreshedWorkflow.nodes.forEach((backendNode) => {
          let localMatch = nodes.find(
            (n) =>
              n.data?.nodeType === backendNode.nodeType &&
              Math.round(n.position.x) === backendNode.positionX &&
              Math.round(n.position.y) === backendNode.positionY
          );

          if (!localMatch && backendNode.isStartNode) {
            localMatch = nodes.find((n) => n.data?.isStartNode);
          }
          if (!localMatch && backendNode.isEndNode) {
            localMatch = nodes.find((n) => n.data?.isEndNode);
          }

          if (localMatch) {
            nodeMap.set(localMatch.id, backendNode.id);
          } else {
            console.warn("Could not map node:", backendNode);
          }
        });

        console.log("Node mapping:", Object.fromEntries(nodeMap));

        const withEdges = {
          ...refreshedWorkflow,
          nodes: refreshedWorkflow.nodes.map((backendNode) => ({
            ...backendNode,
            outgoingNodes: edges
              .filter(
                (e) =>
                  nodeMap.has(e.source) &&
                  nodeMap.has(e.target) &&
                  nodeMap.get(e.source) === backendNode.id
              )
              .map((e) => {
                const targetId = nodeMap.get(e.target)!;
                const targetNode = refreshedWorkflow.nodes.find(n => n.id === targetId);
                return {
                  id: targetId,
                  nodeType: targetNode?.nodeType,
                  eventType: targetNode?.eventType
                };
              }),
          })),
        };

        const finalWorkflow = await updateWorkflow(workflowId, withEdges);
        span.addEvent("workflow_saved_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();

        // Check if workflow is ready to use
        if (finalWorkflow.isReadyToUse === false) {
          setShowWarning(true);
          setTimeout(() => setShowWarning(false), 5000);
        } else {
          setShowSuccess(true);
          setTimeout(() => setShowSuccess(false), 3000);
        }
      } catch (error) {
        console.error("Error saving workflow:", error);
        setShowFail(true);
        span.setAttribute("api.success", false);
        span.recordException(error as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(error) });
        span.addEvent("workflow_save_failed");
        span.end();
        setTimeout(() => setShowFail(false), 4000);
      }
    });
  };

  return (
    <div className="h-screen bg-[#F9FAFB] flex flex-col overflow-hidden">
      <header className="bg-white shadow p-4 flex items-center justify-between shrink-0 relative z-30">
        <div className="flex items-center gap-10">
          <BackButton
            to={id ? `/app/campaigns/${id}` : "/app/campaigns"}
            label={id ? "Back to Campaign Details" : "Back to Campaigns"}
          />
          <h1 className="text-xl font-semibold text-gray-800">
            Workflow Builder – {campaignName ? campaignName : `#${id}`} Campaign
          </h1>
        </div>

        {hasRole("MARKETING_MANAGER") && (
          <div className="flex items-center gap-3">
            <button
              onClick={handleSave}
              className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-4 py-2 rounded-md shadow transition"
            >
              <Save size={16} />
              Save
            </button>
          </div>
        )}
      </header>

      {showSuccess && (
        <SuccessMessage
          entity="Workflow saved"
          onClose={() => setShowSuccess(false)}
        />
      )}
      {showFail && (
        <FailMessage
          entity="Failed to save workflow!"
          onClose={() => setShowFail(false)}
        />
      )}
      {showWarning && (
        <WarningMessage
          message="Workflow saved but is incomplete. All nodes must have required fields and be connected."
          onClose={() => setShowWarning(false)}
        />
      )}

      <div className="flex flex-1 overflow-hidden">
        <div className="flex flex-col grow overflow-hidden">
          <div className="grow overflow-hidden">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onNodesDelete={handleNodesDelete}
              onEdgesChange={onEdgesChange}
              onConnect={onConnect}
              onNodeClick={onNodeClick}
              fitView
            >
              <MiniMap />
              <Controls />
              <Background color="#e5e7eb" gap={16} />
            </ReactFlow>
          </div>
          {hasRole("MARKETING_MANAGER") && (
            <WorkflowBlocksBar onAddNode={handleAddNode} />
          )}
        </div>
        {hasRole("MARKETING_MANAGER") && (
          <aside className="w-96 border-l border-gray-200 bg-white flex flex-col h-full relative z-10">
            <SidebarProperties
              selectedNode={selectedNode}
              onUpdateNode={handleUpdateNode}
              campaignId={Number(id)}
            />
          </aside>
        )}
      </div>
    </div>
  );
};

export default WorkflowBuilder;