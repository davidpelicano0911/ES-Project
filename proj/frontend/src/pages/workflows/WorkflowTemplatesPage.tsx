import { useState, useEffect } from "react";
import EmptyState from "../../components/states/EmptyState";
import { Workflow, Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import LoadingState from "../../components/states/LoadingState";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import SearchBar from "../../components/searchandfilters/SearchBar";
import WorkflowTemplateCard from "../../components/cards/WorkflowTemplateCard";
import { useUser } from "../../context/UserContext";
import { type WorkflowTemplate } from "../../types/workflow";
import {
  getWorkflowTemplates,
  deleteWorkflowTemplate,
} from "../../api/apiWorkflowTemplates";
import { useFlags } from "flagsmith/react";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const WorkflowTemplatesPage = () => {
  const [workflows, setWorkflows] = useState<WorkflowTemplate[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { enable_workflow_template_creation } = useFlags(["enable_workflow_template_creation"]);
  const { enable_workflow_template_search } = useFlags(["enable_workflow_template_search"]);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);

  const { hasRole } = useUser();
  const navigate = useNavigate();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [workflowToDelete, setWorkflowToDelete] = useState<WorkflowTemplate | null>(
    null
  );
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.WorkflowTemplates.Load", (span) => {
      span.setAttribute("page.name", "WorkflowTemplatesPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    const fetchWorkflows = async () => {
      tracer.startActiveSpan("API.GetWorkflowTemplates", async (span) => {
        try {
          setLoading(true);
          const data = await getWorkflowTemplates();
          setWorkflows(data || []);
          span.addEvent("workflow_templates_fetched");
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
        } catch (err) {
          console.error("Failed to fetch workflows", err);
          setError("Failed to load workflow templates");
          span.addEvent("workflow_templates_fetch_failed");
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
        } finally {
          setLoading(false);
        }
      });
    };

    fetchWorkflows();
  }, []);

  const filtered = workflows.filter((w) =>
    tracer.startActiveSpan("User.ACTION.FilterWorkflowTemplates", (span) => {
      const matches = w.name.toLowerCase().includes(search.toLowerCase());
      span.setAttribute("search.query", search);
      span.setAttribute("search.result", matches);
      span.end();
      return matches;
    })
  );

  const handleDelete = (workflow: WorkflowTemplate) => {
    tracer.startActiveSpan("User.ACTION.DeleteWorkflowTemplate", (span) => {
      setWorkflowToDelete(workflow);
      setIsDeleteModalOpen(true);
      span.addEvent("delete_modal_opened");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeleteWorkflowTemplate", async (span) => {
      if (!workflowToDelete) return;
      try {
        await deleteWorkflowTemplate(workflowToDelete.id);
        setWorkflows((prev) => prev.filter((w) => w.id !== workflowToDelete.id));
        setIsDeleteModalOpen(false);
        span.addEvent("delete_modal_closed");
        setToastMessage({
          type: "success",
          text: `Workflow "${workflowToDelete.name}" deleted`,
        });
        span.addEvent("workflow_template_deleted_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
      } catch (err) {
        console.error("Failed to delete workflow", err);
        setToastMessage({
          type: "fail",
          text: `Failed to delete "${workflowToDelete.name}"!`,
        });
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("workflow_template_delete_failed");
      } finally {
        setTimeout(() => setToastMessage(null), 3000);
        span.addEvent("delete_modal_closed");
      }
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col relative">
      {toastMessage && (
        <>
          {toastMessage.type === "success" ? (
            <SuccessMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          ) : (
            <FailMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          )}
        </>
      )}

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mx-8 mt-4 rounded-2xl flex justify-between items-center">
        <div>
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Workflow Templates
          </h1>
          <p className="text-[#6B7280] text-sm">
            Create, edit and manage your automated workflow templates.
          </p>
        </div>

        {enable_workflow_template_creation.enabled && hasRole("MARKETING_MANAGER") && (
          <button
            onClick={() => navigate("/app/workflow-templates/create")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Workflow Template
          </button>
        )}
      </div>

      <div className="flex-1 p-8">
        <div className="mb-6 flex justify-start">
          {enable_workflow_template_search.enabled && (
            <SearchBar
              value={search}
              onChange={setSearch}
              placeholder="Search workflow templates..."
            />
          )}
        </div>

        {loading ? (
          <LoadingState message="Loading workflow templates..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : filtered.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
            <EmptyState
              icon={<Workflow size={48} />}
              title="No Workflow Templates Found"
              description="No workflow templates were found. Try adjusting your search or create a new one."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((w) => (
              <WorkflowTemplateCard
                key={w.id}
                workflow={w}
                onDelete={handleDelete}
              />
            ))}
          </div>
        )}
      </div>

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete Workflow"
        entityName={workflowToDelete?.name}
        message={`Are you sure you want to permanently delete "${
          workflowToDelete?.name || "this workflow"
        }"? This action cannot be undone.`}
        confirmText="Delete Workflow"
        cancelText="Cancel"
        onConfirm={() => void confirmDelete()}
        onCancel={() => setIsDeleteModalOpen(false)}
      />
    </div>
  );
};

export default WorkflowTemplatesPage;
