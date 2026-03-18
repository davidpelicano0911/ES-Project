import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import BackButton from "../../components/buttons/BackButton";
import { createPost } from "../../api/apiPosts";
import facebookImage from "../../assets/facebook.png";
import instagramImage from "../../assets/instagram.png";
import twitterImage from "../../assets/twitter.svg";
import ImageUploader from "./ImageUploader";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateSocialPostsPage = () => {
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [selectedPlatforms, setSelectedPlatforms] = useState<string[]>([]);
  const [publishOption, setPublishOption] = useState<"now" | "schedule">("now");
  const [scheduledDate, setScheduledDate] = useState<string>(""); // ISO string
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const tracer = useTracer();
  const { hasRole } = useUser();

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateSocialPost.Load", (span) => {
      span.setAttribute("page.name", "CreateSocialPostsPage");
      span.setAttribute("user.hasRole.CONTENT_MARKETER", hasRole("CONTENT_MARKETER"));
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  const handleSubmit = (e: React.FormEvent) => {
    tracer.startActiveSpan("User.ACTION.SubmitCreateSocialPostForm", (span) => {
      e.preventDefault();
      // If scheduling, ensure the chosen date is in the future (client-side guard)
      if (publishOption === "schedule") {
        if (!scheduledDate) {
          alert("Please select a scheduled date");
          span.addEvent("scheduled_date_missing");
          span.end();
          return;
        }
        const chosen = new Date(scheduledDate).getTime();
        const now = Date.now();
        if (isNaN(chosen) || chosen <= now) {
          alert("Scheduled date must be in the future. Please pick a later date/time.");
          span.addEvent("scheduled_date_invalid");
          span.end();
          return;
        }
      }

      setShowConfirmModal(true);
      span.addEvent("confirmation_modal_opened");
      span.end();
    });
  };

  const handleConfirm = async () => {
    tracer.startActiveSpan("API.CreateSocialPost", async (span) => {
      const formData = new FormData();
      formData.append("name", name);
      formData.append("description", description);
      selectedPlatforms.forEach(p => formData.append("platforms", p));
      formData.append("image", selectedFile!);
      formData.append("scheduled_date",
        publishOption === "schedule"
          ? new Date(scheduledDate).toISOString().slice(0, 19)
          : new Date(Date.now() + 10 * 1000).toISOString().slice(0, 19)
      );
      const payload = formData;
      span.setAttribute("post.name", name);
      span.setAttribute("post.platforms", selectedPlatforms.join(","));
      span.setAttribute("post.scheduled_date", publishOption === "schedule" ? scheduledDate : "immediate");
      console.log("Submitting post payload:", payload);

      try {
        await createPost(payload as any);
        setSuccessMessageVisible(true);
        span.addEvent("post_created_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        setTimeout(() => navigate("/app/social-posts"), 1500);
      } catch (err: any) {
        // show message using existing UI
        setFailMessageVisible(true);
        span.addEvent("post_creation_failed");
        span.setAttribute("api.success", false);
        span.recordException(err);
        span.setStatus({ code: SpanStatusCode.ERROR, message: err.message });
        setTimeout(() => setFailMessageVisible(false), 3000);
      } finally {
        setShowConfirmModal(false);
        span.addEvent("confirmation_modal_closed");
        span.end();
      }
    });
  };

  const handleCancel = () => {
    tracer.startActiveSpan("User.ACTION.CancelCreateSocialPost", (span) => {
      setShowConfirmModal(false);
      span.addEvent("confirmation_modal_closed");
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {successMessageVisible && <SuccessMessage entity="Post created" />}
      {failMessageVisible && <FailMessage entity="Post creation" />}
      {showConfirmModal && (
        <ConfirmationModal
          title="Confirm Post Creation"
          message="Are you sure you want to create this post?"
          confirmText="Yes, create"
          cancelText="No, go back"
          onConfirm={handleConfirm}
          onCancel={handleCancel}
        />
      )}

      <BackButton to="/app/social-posts" label="Back to Social Posts" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Create Social Media Post
          </h1>
          <p className="text-[#6B7280] text-sm">
            Schedule and post your social media content.
          </p>
        </div>
      </div>

      <div className="mt-8">
        <div className="bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-[#111827] mb-1">
                Post Name <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter post name"
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
              <label className="block text-sm font-medium text-[#111827] mb-2">
                Upload Image <span className=""></span>
              </label>
              <ImageUploader onFileSelect={setSelectedFile} />
            </div>
            

            <div>
              <label className="block text-sm font-medium text-[#111827] mb-2">
                Platforms <span className="text-red-500">*</span>
              </label>

              <div className="grid sm:grid-cols-2 md:grid-cols-3 gap-3">
                {[
                  { name: "facebook", icon: facebookImage },
                  { name: "instagram", icon: instagramImage },
                  { name: "twitter", icon: twitterImage },
                ].map((platform) => (
                  <label
                    key={platform.name}
                    className="flex items-center gap-3 border border-[#E5E7EB] rounded-lg px-3 py-2 hover:bg-[#F9FAFB] cursor-pointer transition capitalize"
                  >
                    <input
                      type="checkbox"
                      value={platform.name}
                      checked={selectedPlatforms.includes(platform.name)}
                      onChange={(e) => {
                        const value = e.target.value;
                        if (e.target.checked) {
                          setSelectedPlatforms([...selectedPlatforms, value]);
                        } else {
                          setSelectedPlatforms(selectedPlatforms.filter((p) => p !== value));
                        }
                      }}
                      required={selectedPlatforms.length === 0}
                      className="w-4 h-4 accent-[#2563EB]"
                    />
                    <img
                      src={platform.icon}
                      alt={platform.name}
                      className="w-6 h-6 rounded"
                    />
                    <span className="text-sm text-[#374151]">{platform.name}</span>
                  </label>
                ))}
              </div>

              <p className="text-xs text-[#6B7280] mt-2">
                Select at least one platform where you want to post your content.
              </p>
            </div>



            {/* Publish Option */}
            <div className="mt-4">
              <label className="block text-sm font-medium text-[#111827] mb-2">
                Publish Options <span className="text-red-500">*</span>
              </label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="publishOption"
                    value="now"
                    checked={publishOption === "now"}
                    onChange={() => setPublishOption("now")}
                    className="w-4 h-4 accent-[#2563EB]"
                  />
                  Publish Now
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="publishOption"
                    value="schedule"
                    checked={publishOption === "schedule"}
                    onChange={() => setPublishOption("schedule")}
                    className="w-4 h-4 accent-[#2563EB]"
                  />
                  Schedule Post
                </label>
              </div>

              {publishOption === "schedule" && (
                <input
                  type="datetime-local"
                  value={scheduledDate}
                  onChange={(e) => setScheduledDate(e.target.value)}
                  required
                  className="mt-2 w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
                />
              )}
            </div>

            {/* Buttons */}
            <div className="flex gap-3 pt-4">
              <button
                type="submit"
                className="cursor-pointer bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium px-5 py-2.5 rounded-lg transition"
              >
                {publishOption === "now" ? "Publish Now" : "Schedule Post"}
              </button>


              <button
                type="button"
                onClick={() => {
                  navigate("/app/social-posts");
                }}
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

export default CreateSocialPostsPage;
