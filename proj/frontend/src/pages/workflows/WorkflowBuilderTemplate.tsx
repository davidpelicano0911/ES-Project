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
import WorkflowBlocksBar from "../../components/WorkflowBlocksBar";
import BackButton from "../../components/buttons/BackButton";
import { getWorkflowTemplateById, updateWorkflowTemplate } from "../../api/apiWorkflowTemplates";
import type { WorkflowTemplate } from "../../types/workflow";
import { useUser } from "../../context/UserContext";
import { useFlags } from "flagsmith/react";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const WorkflowBuilderTemplate = () => {
  const { id } = useParams();
  const [nodes, setNodes, onNodesChange] = useNodesState<RFNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<RFEdge>([]);
  const [selectedNode, setSelectedNode] = useState<RFNode | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showFail, setShowFail] = useState(false);
  const [workflowName, setWorkflowName] = useState<string>("");
  const { hasRole } = useUser();
  const { enable_workflow_template_editing } = useFlags(["enable_workflow_template_editing"]);
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.WorkflowBuilderTemplate.Load", (span) => {
      span.setAttribute("page.name", "WorkflowBuilderTemplate");
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
      default:
        return { label: nodeType || "Node", color: "#2563EB" };
    }
  };

  useEffect(() => {
    const fetchTemplate = async () => {
      tracer.startActiveSpan("API.GetWorkflowTemplateById", async (span) => {
        if (!id) {
          span.setStatus({ code: SpanStatusCode.ERROR, message: "No template ID provided" });
          span.end();
          return;
        }
        try {
          const template: WorkflowTemplate = await getWorkflowTemplateById(Number(id));
          setWorkflowName(template.name);

          if (template.templateData) {
            const { nodes, edges } = JSON.parse(template.templateData);
            setNodes(nodes || []);
            setEdges(edges || []);
          } else {
            const startColor = "#22C55E";
            setNodes([
              {
                id: `${Date.now()}-start`,
                type: "input",
                data: { label: "Start", nodeType: "START", isStartNode: true },
                position: { x: 150, y: 200 },
                style: {
                  backgroundColor: startColor,
                  color: "#fff",
                  padding: "12px 16px",
                  borderRadius: 10,
                  fontWeight: 500,
                  textAlign: "center",
                  boxShadow: "0 2px 6px rgba(0,0,0,0.15)",
                },
              },
            ]);
            setEdges([]);
          }
          span.setAttribute("api.success", true);
          span.setAttribute("template.id", id);
          span.addEvent("workflow_template_fetched_successfully");
          span.setStatus({ code: SpanStatusCode.OK });
          span.end();
        } catch (err) {
          console.error("Error fetching template:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          span.addEvent("workflow_template_fetch_failed");
          span.end();
        }
      });
    };

    fetchTemplate();
  }, [id, setNodes, setEdges]);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  const onNodeClick = useCallback((_: any, node: RFNode) => {
    tracer.startActiveSpan("User.ACTION.SelectNode", (span) => {
      setSelectedNode(node);
      span.setAttribute("node.id", node.id);
      span.addEvent("node_clicked");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  }, []);

  const handleAddNode = useCallback(
    (nodeType: string) => {
      tracer.startActiveSpan("User.ACTION.AddNodeInWorkflowTemplateBuilder", (span) => {
        const { label, color } = getNodeAppearance(nodeType);
        const newId = `${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
        const newNode: RFNode = {
          id: newId,
          data: { label, color, nodeType },
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
        span.setAttribute("node.id", newId);
        span.setAttribute("node.type", nodeType);
        span.addEvent("node_added");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      });
    },
    [nodes.length, setNodes]
  );

  const handleUpdateNode = useCallback(
    (field: string, value: any) => {
      tracer.startActiveSpan("User.ACTION.UpdateNodeInWorkflowTemplateBuilder", (span) => {
        if (!selectedNode) {
          return;
        }
        const updatedNode: RFNode = {
          ...selectedNode,
          data: { ...selectedNode.data, [field]: value },
        };
        setNodes((nds) =>
          nds.map((n) => (n.id === selectedNode.id ? updatedNode : n))
        );
        setSelectedNode(updatedNode);
        span.setAttribute("node.id", updatedNode.id);
        span.setAttribute("node.field", field);
        span.addEvent("node_updated");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      });
    },
    [selectedNode, setNodes]
  );

  const handleNodesDelete = useCallback(
    (deleted: RFNode[]) => {
      tracer.startActiveSpan("User.ACTION.DeleteNodeInWorkflowTemplateBuilder", (span) => {
        setNodes((nds) => nds.filter((n) => !deleted.some((d) => d.id === n.id)));
        setEdges((eds) =>
          eds.filter(
            (e) => !deleted.some((d) => d.id === e.source || d.id === e.target)
          )
        );
        if (selectedNode && deleted.some((d) => d.id === selectedNode.id)) {
          setSelectedNode(null);
        }
        span.setAttribute("deleted.node.count", deleted.length);
        span.addEvent("nodes_deleted");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      });
    },
    [selectedNode, setNodes, setEdges]
  );

  const handleSave = async () => {
    tracer.startActiveSpan("API.UpdateWorkflowTemplate", async (span) => {
      try {
        const data = JSON.stringify({ nodes, edges });
        await updateWorkflowTemplate(Number(id), {
          templateData: data,
        });

        span.addEvent("workflow_template_saved_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
        setShowSuccess(true);
        setTimeout(() => setShowSuccess(false), 3000);
      } catch (err) {
        console.error("Error saving template:", err);
        setShowFail(true);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("workflow_template_save_failed");
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
            to="/app/workflow-templates"
            label={id ? "Back to Campaign Details" : "Back to Campaigns"}
          />
          <h1 className="text-xl font-semibold text-gray-800">
            Workflow Template – {workflowName || `#${id}`}
          </h1>
        </div>

        {enable_workflow_template_editing.enabled && hasRole("MARKETING_MANAGER") && (
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
        {enable_workflow_template_editing.enabled && hasRole("MARKETING_MANAGER") && (
          <aside className="w-96 border-l border-gray-200 bg-white flex flex-col h-full relative z-10">
            <SidebarProperties
              selectedNode={selectedNode}
              onUpdateNode={handleUpdateNode}
            />
          </aside>
        )}
      </div>
    </div>
  );
};

export default WorkflowBuilderTemplate;
