import { useEffect, useState } from "react";
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  type Node as RFNode,
  type Edge as RFEdge,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { useParams } from "react-router-dom";
import BackButton from "../../components/buttons/BackButton";
import { getWorkflowById } from "../../api/apiWorkflows";
import type { Workflow } from "../../types/workflow";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const WorkflowPreviewPage = () => {
  const { id } = useParams();
  const [workflow, setWorkflow] = useState<Workflow | null>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<RFNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<RFEdge>([]);
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.WorkflowPreview.Load", (span) => {
      span.setAttribute("page.name", "WorkflowPreviewPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

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
      default:
        return { label: nodeType || "Node", color: "#2563EB" };
    }
  };

  useEffect(() => {
    const fetchWorkflow = async () => {
      tracer.startActiveSpan("API.GetWorkflowById", async (span) => {
        if (!id) {
          span.setStatus({ code: SpanStatusCode.ERROR, message: "No workflow ID provided" });
          span.end();
          return;
        }

        try {
          const wf = await getWorkflowById(Number(id));
          setWorkflow(wf);

          const formattedNodes: RFNode[] = wf.nodes.map((n) => {
            const { label, color } = getNodeAppearance(
              n.nodeType,
              n.isStartNode,
              n.isEndNode
            );
            return {
              id: n.id.toString(),
              position: { x: n.positionX, y: n.positionY },
              data: { label },
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

          const formattedEdges: RFEdge[] = wf.nodes.flatMap((n) =>
            (n.outgoingNodes || []).map((target) => ({
              id: `edge-${n.id}-${target.id}`,
              source: n.id.toString(),
              target: target.id.toString(),
            }))
          );

          setNodes(formattedNodes);
          setEdges(formattedEdges);
          span.addEvent("workflow_fetched_successfully");
          span.setAttribute("workflow.id", id);
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          span.end();
        } catch (err) {
          console.error("Error fetching workflow:", err);
          span.setAttribute("workflow.id", id);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
          span.addEvent("workflow_fetch_failed");
          span.end();
        }
      });
    };

    fetchWorkflow();
  }, [id, setNodes, setEdges]);

  return (
    <div className="h-screen bg-[#F9FAFB] flex flex-col overflow-hidden">
      <header className="bg-white shadow p-5 flex flex-col gap-2 shrink-0 relative z-30">
        <div className="flex items-center gap-10">
          <BackButton
            to="/app/workflow-templates"
            label="Back to Workflow Templates"
          />
          <h1 className="text-2xl font-semibold text-gray-800">
            {workflow?.name || "Workflow Preview"}
          </h1>
        </div>
      </header>

      <div className="flex flex-1 overflow-hidden">
        <div className="flex flex-col grow overflow-hidden">
          <div className="grow overflow-hidden">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              nodesDraggable={false}
              nodesConnectable={false}
              elementsSelectable={false}
              panOnDrag={true}
              zoomOnScroll={true}
              fitView
            >
              <MiniMap />
              <Controls />
              <Background color="#e5e7eb" gap={16} />
            </ReactFlow>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WorkflowPreviewPage;
