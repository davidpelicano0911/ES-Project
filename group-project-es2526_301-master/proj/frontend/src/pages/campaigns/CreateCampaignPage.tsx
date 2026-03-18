import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import BackButton from "../../components/buttons/BackButton";
import { getSegments } from "../../api/apiSegments";
import { createCampaign } from "../../api/apiCampaigns";
import type { Segment } from "../../types/segment";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateCampaignPage = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [dueDate, setDueDate] = useState("");
  const [selectedSegments, setSelectedSegments] = useState<number[]>([]);
  const [segments, setSegments] = useState<Segment[]>([]);
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateCampaign.Load", (span) => {
      span.setAttribute("page.name", "CreateCampaignPage");
      span.setAttribute("user.role.MARKETING_MANAGER", hasRole("MARKETING_MANAGER"));
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer, hasRole]);

  useEffect(() => {
    const fetchSegments = async () => {
      tracer.startActiveSpan("API.GetSegments", async (span) => {
        try {
          const data = await getSegments();
          span.setAttribute("api.success", true);
          span.addEvent("segments_fetched");
          span.setStatus({ code: SpanStatusCode.OK });
          setSegments(data);
        } catch (err) {
          console.error("Error fetching segments:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          setFailMessageVisible(true);
          setTimeout(() => setFailMessageVisible(false), 3000);
        } finally {
          span.end();
        }
      });
    };
    fetchSegments();
  }, [tracer]);

  const handleSegmentToggle = (id: number) => {
    tracer.startActiveSpan("User.ACTION.ToggleSegment", (span) => {
      span.setAttribute("segment.id", id);
      span.addEvent("toggle_segment_initiated");

      setSelectedSegments((prev) =>
        prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]
      );

      span.addEvent("toggle_segment_completed");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    tracer.startActiveSpan("User.ACTION.SubmitCreateCampaign", (span) => {
      e.preventDefault();

      if (!name || !description || !dueDate || selectedSegments.length === 0) {
        span.setAttribute("form.validation", "failed");
        span.addEvent("campaign_submit_failed");
        span.setStatus({ code: SpanStatusCode.ERROR });
        alert("Please fill out all fields before proceeding.");
        span.end();
        return;
      }
      setShowConfirmModal(true);
      span.setAttribute("form.validation", "passed");
      span.addEvent("campaign_submit_completed");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  const handleConfirm = async () => {
    const payload = {
      name,
      description,
      segment: selectedSegments.map((id) => ({ id })),
      createdAt: new Date().toISOString(),
      dueDate: new Date(dueDate).toISOString(),
      status: "IN_PROGRESS" as const,
    };
    tracer.startActiveSpan("API.CreateCampaign", async (span) => {
      try {
        await createCampaign(payload);
        span.setAttribute("api.success", true);
        span.addEvent("campaign_created");
        span.setStatus({ code: SpanStatusCode.OK });
        setSuccessMessageVisible(true);
        setTimeout(() => navigate("/app/campaigns"), 1500);
      } catch (err) {
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        console.error("Error creating campaign:", err);
        setFailMessageVisible(true);
        setTimeout(() => setFailMessageVisible(false), 3000);
      } finally {
        setShowConfirmModal(false);
        span.end();
      }
    });
  };

  const handleCancel = () => {
    tracer.startActiveSpan("User.ACTION.CancelCreateCampaign", (span) => {
      setShowConfirmModal(false);
      span.addEvent("cancelled_campaign_creation");
      span.setStatus({ code: SpanStatusCode.OK });
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {successMessageVisible && <SuccessMessage entity="Campaign created" />}
      {failMessageVisible && <FailMessage entity="Campaign creation" />}
      {showConfirmModal && (
        <ConfirmationModal
          title="Confirm Campaign Creation"
          message="Are you sure you want to create this campaign?"
          confirmText="Yes, create"
          cancelText="No, go back"
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}

      <BackButton to="/app/campaigns" label="Back to Campaigns" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Create Campaign
          </h1>
          <p className="text-[#6B7280] text-sm">
            Set up a new marketing campaign to reach your target audience
          </p>
        </div>
      </div>

      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Campaign Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter campaign name"
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Description <span className="text-red-500">*</span>
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Describe your campaign goals and strategy..."
                rows={4}
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Due Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                value={dueDate}
                onChange={(e) => setDueDate(e.target.value)}
                required
                className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-2">
                Target Segments <span className="text-red-500">*</span>
              </label>

              <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-3">
                {segments.map((segment) => (
                  <label
                    key={segment.id}
                    className="flex items-center gap-2 border border-[#E5E7EB] rounded-lg px-3 py-2 hover:bg-[#F9FAFB] cursor-pointer transition"
                  >
                    <input
                      type="checkbox"
                      checked={selectedSegments.includes(segment.id)}
                      onChange={() => handleSegmentToggle(segment.id)}
                      className="w-4 h-4 accent-[#2563EB]"
                    />
                    <span className="text-sm text-[#374151]">
                      {segment.name}
                    </span>
                  </label>
                ))}
              </div>

              <p className="text-xs text-[#6B7280] mt-2">
                Select one or more segments that fit your target audience.
              </p>
            </div>

            <div className="flex gap-3 pt-4">
              <button
                type="submit"
                className="cursor-pointer bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium px-5 py-2.5 rounded-lg transition"
              >
                Add Campaign
              </button>

              <button
                type="button"
                onClick={() => navigate("/app/campaigns")}
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

export default CreateCampaignPage;
