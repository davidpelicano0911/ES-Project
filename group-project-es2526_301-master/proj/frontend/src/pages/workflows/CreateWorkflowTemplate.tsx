import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Plus, Loader2 } from "lucide-react";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import BackButton from "../../components/buttons/BackButton";
import { createWorkflowTemplate } from "../../api/apiWorkflowTemplates";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateWorkflowTemplate = () => {
  const navigate = useNavigate();

  const [workflowName, setWorkflowName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateWorkflowTemplate.Load", (span) => {
      span.setAttribute("page.name", "CreateWorkflowTemplate");
      span.setAttribute("user.hasRole.MARKETING_MANAGER", hasRole("MARKETING_MANAGER"));
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer, hasRole]);

  const handleSubmit = (e: React.FormEvent) => {
    tracer.startActiveSpan("User.ACTION.SubmitCreateWorkflowTemplateForm", (span) => {
      e.preventDefault();
      if (!workflowName.trim()) {
        alert("Please fill out all required fields before proceeding.");
        span.addEvent("form_validation_failed");
        span.setStatus({ code: SpanStatusCode.ERROR, message: "Validation failed" });
        span.end();
        return;
      }
      setShowConfirmModal(true);
      span.addEvent("confirmation_modal_shown");
      span.addEvent("form_validation_passed");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  const handleConfirm = async () => {
    tracer.startActiveSpan("API.CreateWorkflowTemplate", async (span) => {
      setLoading(true);
      try {
        await createWorkflowTemplate({
          name: workflowName,
          description,
          createdAt: new Date().toISOString(),
        });

        setSuccessMessageVisible(true);
        span.addEvent("workflow_template_created_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });

        setTimeout(() => {
          navigate("/app/workflow-templates");
        }, 1500);
      } catch (error) {
        console.error("Error creating workflow template:", error);
        span.setAttribute("api.success", false);
        span.recordException(error as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(error) });
        span.addEvent("workflow_template_creation_failed");
        setFailMessageVisible(true);
        setTimeout(() => setFailMessageVisible(false), 3000);
      } finally {
        setShowConfirmModal(false);
        span.addEvent("confirmation_modal_closed");
        setLoading(false);
        span.end();
      }
    });
  };

  const handleCancel = () => { 
    tracer.startActiveSpan("User.ACTION.CancelCreateWorkflowTemplate", (span) => {
      setShowConfirmModal(false);
      span.addEvent("confirmation_modal_closed");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {successMessageVisible && <SuccessMessage entity="Workflow template created" />}
      {failMessageVisible && <FailMessage entity="Workflow template creation" />}
      {showConfirmModal && (
        <ConfirmationModal
          title="Confirm Workflow Template Creation"
          message="Are you sure you want to create this workflow template?"
          confirmText="Yes, create"
          cancelText="No, go back"
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}

      <BackButton
        to="/app/workflow-templates"
        label="Back to Workflow Templates"
      />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Create Workflow Template
          </h1>
          <p className="text-[#6B7280] text-sm mt-1">
            Build a reusable workflow template for your marketing automation needs.
          </p>
        </div>
      </div>

      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Template Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={workflowName}
                onChange={(e) => setWorkflowName(e.target.value)}
                placeholder="Enter template name"
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 
                           focus:outline-none focus:ring-2 focus:ring-[#2563EB] 
                           placeholder:text-gray-400"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Description
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe the purpose and use cases for this workflow template..."
                rows={4}
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 resize-none 
                           focus:outline-none focus:ring-2 focus:ring-[#2563EB] 
                           placeholder:text-gray-400"
              />
            </div>

            <div className="flex flex-wrap gap-3 pt-4">
              <button
                type="submit"
                disabled={loading}
                className="cursor-pointer bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium 
                           px-5 py-2.5 rounded-lg transition flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-4 h-4 animate-spin" />
                    Creating...
                  </>
                ) : (
                  <>
                    <Plus className="w-4 h-4" />
                    Create Template
                  </>
                )}
              </button>

              <button
                type="button"
                onClick={() => navigate("/app/workflow-templates")}
                className="cursor-pointer border border-[#E5E7EB] text-[#374151] 
                           px-5 py-2.5 rounded-lg hover:bg-[#F3F4F6] transition"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateWorkflowTemplate;