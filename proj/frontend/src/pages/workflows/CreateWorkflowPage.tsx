import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { Plus, Upload, Loader2 } from "lucide-react";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import BackButton from "../../components/buttons/BackButton";
import { createWorkflow } from "../../api/apiWorkflows";
import ImportWorkflowModal from "../../components/modals/ImportWorkflowModal";
import { useFlags } from "flagsmith/react";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateWorkflowPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const campaignId = queryParams.get("campaignId");
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [workflowName, setWorkflowName] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { enable_workflow_template_import } = useFlags(["enable_workflow_template_import"]);
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateWorkflow.Load", (span) => {
      span.setAttribute("page.name", "CreateWorkflowPage");
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
    tracer.startActiveSpan("User.ACTION.SubmitCreateWorkflowForm", (span) => {
      e.preventDefault();
      if (!workflowName.trim()) {
        alert("Please fill out all required fields before proceeding.");
        span.setAttribute("form.validation", "failed");
        span.addEvent("form_validation_failed");
        span.setStatus({ code: SpanStatusCode.ERROR, message: "Validation failed" });
        span.end();
        return;
      }
      setShowConfirmModal(true);
      span.addEvent("confirmation_modal_shown");
      span.setAttribute("form.validation", "passed");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  const handleConfirm = async () => {
    tracer.startActiveSpan("API.CreateWorkflow", async (span) => {
      setLoading(true);
      try {
        await createWorkflow({
          name: workflowName,
          description,
          createdAt: new Date().toISOString(),
          campaignId: campaignId ? Number(campaignId) : null,
        });

        setSuccessMessageVisible(true);

        span.addEvent("workflow_created_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });

        setTimeout(() => {
          if (campaignId) {
            navigate(`/app/campaigns/${campaignId}`, {
              state: { hasWorkflow: true },
            });
          } else {
            navigate("/app/campaigns");
          }
        }, 1500);
      } catch (error) {
        console.error("Error creating workflow:", error);
        span.addEvent("workflow_creation_failed");
        span.setAttribute("api.success", false);
        span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
        span.recordException(error as Error);
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
    tracer.startActiveSpan("User.ACTION.CancelCreateWorkflow", (span) => {
      setShowConfirmModal(false);
      span.addEvent("confirmation_modal_closed");
      span.addEvent("workflow_creation_cancelled");
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {successMessageVisible && <SuccessMessage entity="Workflow created" />}
      {failMessageVisible && <FailMessage entity="Workflow creation" />}
      {showConfirmModal && (
        <ConfirmationModal
          title="Confirm Workflow Creation"
          message="Are you sure you want to create this workflow?"
          confirmText="Yes, create"
          cancelText="No, go back"
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}

      <BackButton
        to={campaignId ? `/app/campaigns/${campaignId}` : "/app/campaigns"}
        label={campaignId ? "Back to Campaign Details" : "Back to Campaigns"}
      />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Create Workflow
          </h1>
          <p className="text-[#6B7280] text-sm mt-1">
            Build a new automation workflow to connect marketing actions
            seamlessly.
          </p>
        </div>
      </div>

      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Workflow Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={workflowName}
                onChange={(e) => setWorkflowName(e.target.value)}
                placeholder="Enter workflow name"
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
                placeholder="Describe the purpose or logic of this workflow..."
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
                    Add Workflow
                  </>
                )}
              </button>
              {enable_workflow_template_import.enabled && (
                <button
                  type="button"
                  onClick={() => setIsImportModalOpen(true)}
                  className="cursor-pointer border border-blue-600 text-blue-600 bg-white 
              hover:bg-blue-50 px-5 py-2.5 rounded-lg flex items-center justify-center gap-2 transition"
                >
                  <Upload className="w-4 h-4 opacity-80" />
                  Import Workflow Template
                </button>
              )}

              <button
                type="button"
                onClick={() =>
                  campaignId
                    ? navigate(`/app/campaigns/${campaignId}`)
                    : navigate("/app/campaigns")
                }
                className="cursor-pointer border border-[#E5E7EB] text-[#374151] 
                           px-5 py-2.5 rounded-lg hover:bg-[#F3F4F6] transition"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
      <ImportWorkflowModal
        isOpen={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
        campaignId={campaignId ? Number(campaignId) : 0}
        onWorkflowImported={(workflow) => {
          console.log("Imported workflow:", workflow);
          setIsImportModalOpen(false);
          navigate(`/app/campaigns/${campaignId || ""}`);
        }}
      />
    </div>
  );
};

export default CreateWorkflowPage;
