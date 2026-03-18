import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import BackButton from "../../components/buttons/BackButton";
import { getCampaigns } from "../../api/apiCampaigns";
import { createDashboard } from "../../api/apiDashboards";
import type { Campaign } from "../../types/campaign";
import { useUser } from "../../context/UserContext";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const CreateCampaignDashboard = () => {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [campaignId, setCampaignId] = useState<number | "">("");
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateCampaignDashboard.Load", (span) => {
      span.setAttribute("page.name", "CreateCampaignDashboard");
      span.setAttribute("user.role.MARKETING_ANALYST", hasRole("MARKETING_ANALYST"));

      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.end();
      };
    });
  }, [hasRole, tracer]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetCampaigns", (span) => {
      const fetchCampaigns = async () => {
        try {
          const campaignsWithoutDashboard = (await getCampaigns()).filter(
            (c) => !c.dashboard
          );
          span.setAttribute("api.success", true);
          span.addEvent("campaigns_fetch_success");
          span.setStatus({ code: SpanStatusCode.OK });
          setCampaigns(campaignsWithoutDashboard);
        } catch (err) {
          console.error("Error fetching campaigns:", err);
          span.setAttribute("api.success", false);
          span.addEvent("campaigns_fetch_failure");
          span.setStatus({ code: SpanStatusCode.ERROR });
          setFailMessageVisible(true);
          setTimeout(() => setFailMessageVisible(false), 3000);
        }
        span.end();
      };
      fetchCampaigns();
    });
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    tracer.startActiveSpan("User.ACTION.SubmitNewDashboard", (span) => {
      e.preventDefault();
      if (!title || campaignId === "") {
        alert("Please fill out all fields before proceeding.");
        span.setAttribute("form.validation", "failed");
        span.addEvent("form_submit_failed");
        span.setStatus({ code: SpanStatusCode.ERROR });
        span.end();
        return;
      }
      span.setAttribute("form.validation", "passed");
      span.addEvent("form_submit_completed");
      span.setStatus({ code: SpanStatusCode.OK });
      setShowConfirmModal(true);
      span.end();
    });
  };

  const handleConfirm = async () => {
    tracer.startActiveSpan("API.CreateDashboard", async (span) => {
      try {
        await createDashboard(Number(campaignId), { title });
        span.setAttribute("api.success", true);
        span.addEvent("dashboard_created");
        span.setStatus({ code: SpanStatusCode.OK });
        setSuccessMessageVisible(true);
        setTimeout(() => navigate("/app/dashboard"), 1500);
      } catch (err) {
        console.error("Error creating dashboard:", err);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        setFailMessageVisible(true);
        setTimeout(() => setFailMessageVisible(false), 3000);
      } finally {
        setShowConfirmModal(false);
        span.end();
      }
    });
  };

  const handleCancel = () => {
    tracer.startActiveSpan("User.ACTION.CancelCreateDashboard", (span) => {
      setShowConfirmModal(false);
      span.addEvent("cancelled_dashboard_creation");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {successMessageVisible && <SuccessMessage entity="Dashboard created" />}
      {failMessageVisible && <FailMessage entity="Dashboard creation" />}
      {showConfirmModal && (
        <ConfirmationModal
          title="Confirm Dashboard Creation"
          message="Are you sure you want to create this dashboard?"
          confirmText="Yes, create"
          cancelText="No, go back"
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}

      <BackButton to="/app/dashboard" label="Back to Dashboards" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Create Dashboard
          </h1>
          <p className="text-[#6B7280] text-sm">
            Link your dashboard to an existing campaign and track performance
          </p>
        </div>
      </div>

      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Dashboard Title <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Enter dashboard title"
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Associated Campaign <span className="text-red-500">*</span>
              </label>
              <select
                value={campaignId}
                onChange={(e) => setCampaignId(Number(e.target.value))}
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
              >
                <option value="" disabled>
                  Select a campaign
                </option>
                {campaigns.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
              <p className="text-xs text-[#6B7280] mt-2">
                Choose the campaign that this dashboard will be linked to.
              </p>
            </div>

            <div className="flex gap-3 pt-4">
              <button
                type="submit"
                className="cursor-pointer bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium px-5 py-2.5 rounded-lg transition"
              >
                Add Dashboard
              </button>

              <button
                type="button"
                onClick={() => navigate("/app/dashboard")}
                className="cursor-pointer border border-[#E5E7EB] text-[#374151] px-5 py-2.5 rounded-lg hover:bg-[#F3F4F6] transition"
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

export default CreateCampaignDashboard;
