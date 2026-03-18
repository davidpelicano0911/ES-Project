import { useState, useRef, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import EmailEditor from "react-email-editor";
import {
  createLandingPage,
  updateLandingPage,
  getLandingPageById,
} from "../../api/apiLandingPages";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import BackButton from "../../components/buttons/BackButton";
import SavePreviewModal from "../../components/modals/SavePreviewModal";
import LoadingState from "../../components/states/LoadingState";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateLandingPage = () => {
  const navigate = useNavigate();
  const { id } = useParams(); 
  const editorRef = useRef<any>(null);

  const [existingPage, setExistingPage] = useState<any>(null);
  const [loading, setLoading] = useState(!!id);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [body, setBody] = useState("");
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showFail, setShowFail] = useState(false);
  const { hasRole } = useUser();
  const tracer = useTracer();

  const projectId = import.meta.env.VITE_UNLAYER_PUBLIC_KEY || "";
  const requiredFilled = name.trim() !== "";

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateLandingPage.Load", (span) => {
      span.setAttribute("page.name", "CreateLandingPage");
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
    tracer.startActiveSpan("API.GetLandingPageById", (span) => {
      const fetchPage = async () => {
        if (!id){
          localStorage.removeItem("landingDesign");
          span.setAttribute("api.success", false);
          span.setStatus({ code: SpanStatusCode.ERROR });
          span.end();
          return;
        }
        try {
          setLoading(true);
          const data = await getLandingPageById(Number(id));
          setExistingPage(data);
          setName(data.name || "");
          setDescription(data.description || "");
          setBody(data.body || "");
          span.setAttribute("api.landingPageId", id);
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          if (data.design) {
            localStorage.setItem("landingDesign", data.design);
          }
        } catch (err) {
          console.error("Failed to load landing page:", err);
          span.recordException(err as Error);
          span.setAttribute("api.success", false);
          span.setStatus({ code: SpanStatusCode.ERROR, message: "Failed to load landing page" });
          setShowFail(true);
        } finally {
          setLoading(false);
        }
      };
      fetchPage();
    });
  }, [id]);

  const exportHtml = () => {
    tracer.startActiveSpan("User.ACTION.PreviewLandingPage", (span) => {
      if (!requiredFilled) return;
      if (!editorRef.current?.editor) return;

      try {
        editorRef.current.editor.exportHtml((data: any) => {
          if (!data?.html) {
            setBody("<p style='color:red'>Failed to export HTML.</p>");
            setIsPreviewModalOpen(true);
            span.addEvent("no_landing_html_exported");
            span.setStatus({ code: SpanStatusCode.ERROR, message: "No HTML exported" });
            span.addEvent("preview_modal_opened");
            span.end();
            return;
          }

          setBody(data.html);
          localStorage.setItem("landingDesign", JSON.stringify(data.design));
          span.addEvent("landing_html_exported_successfully");
          span.setStatus({ code: SpanStatusCode.OK });
          setIsPreviewModalOpen(true);
          span.addEvent("preview_modal_opened");
          span.end();
        });
      } catch (err: any) {
        setBody(`<p style='color:red'>Failed to export HTML. ${err.message}</p>`);
        setIsPreviewModalOpen(true);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: "Failed to export HTML" });
        span.addEvent("landing_html_export_failed");
        span.addEvent("preview_modal_opened");
        span.end();
      }
    });
  };

  const handleEditorReady = () => {
    if (!editorRef.current?.editor) return;

    const storedDesign = localStorage.getItem("landingDesign");
    const designToLoad = storedDesign || existingPage?.design;
    if (designToLoad) {
      try {
        const parsedDesign =
          typeof designToLoad === "string"
            ? JSON.parse(designToLoad)
            : designToLoad;
        editorRef.current.editor.loadDesign(parsedDesign);
      } catch (err) {
        console.warn("Invalid design format", err);
      }
    }
  };

  const savePage = async () => {
    tracer.startActiveSpan("API.SaveLandingPage", async (span) => {
      const design = localStorage.getItem("landingDesign") || existingPage?.design;
      const payload = { name, description, body, design };

      try {
        if (existingPage) {
          await updateLandingPage(existingPage.id, payload);
          span.setAttribute("landingPage.id", existingPage.id);
          span.addEvent("landing_page_updated");
        } else {
          await createLandingPage(payload);
          span.addEvent("landing_page_created");
        }

        setShowSuccess(true);
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        setTimeout(() => navigate("/app/landing-pages"), 1500);
      } catch (err) {
        console.error("Failed to save landing page:", err);
        span.recordException(err as Error);
        span.setAttribute("api.success", false);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("landing_page_save_failed");
        setShowFail(true);
        setTimeout(() => setShowFail(false), 3000);
      } finally {
        localStorage.removeItem("landingDesign");
        setIsPreviewModalOpen(false);
        span.end();
      }
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F9FAFB]">
        <LoadingState message="Loading landing page..." />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {showSuccess && (
        <SuccessMessage
          entity={existingPage ? "Landing page updated" : "Landing page created"}
          onClose={() => setShowSuccess(false)}
        />
      )}
      {showFail && (
        <FailMessage
          entity="Landing page saving failed"
          onClose={() => setShowFail(false)}
        />
      )}

      <BackButton to="/app/landing-pages" label="Back to Landing Pages" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            {existingPage ? "Edit Landing Page" : "Create Landing Page"}
          </h1>
          <p className="text-[#6B7280] text-sm">
            Design and customize your landing page.
          </p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => navigate("/app/landing-pages")}
            className="border border-[#E5E7EB] text-[#374151] hover:bg-[#F9FAFB] px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm transition-all"
          >
            Cancel
          </button>

          <button
            onClick={exportHtml}
            disabled={!requiredFilled}
            className={`px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm transition-all active:scale-[0.98] ${
              requiredFilled
                ? "bg-[#2563EB] hover:bg-[#1D4ED8] text-white cursor-pointer"
                : "bg-gray-300 text-gray-500 cursor-not-allowed"
            }`}
          >
            Preview Landing Page
          </button>
        </div>
      </div>

      <div className="mt-8 bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
        <div className="space-y-6 mb-6">
          <div>
            <label className="block text-sm font-medium text-[#111827] mb-1">
              Landing Page Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Example: Summer Campaign Landing Page"
              className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-[#111827] mb-1">
              Description <span className="text-gray-400">(optional)</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Briefly describe the purpose of this landing page..."
              rows={3}
              className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
            />
          </div>
        </div>

        <div className="h-[650px] border border-[#E5E7EB] rounded-lg overflow-hidden shadow-sm">
          <EmailEditor
            ref={editorRef}
            projectId={projectId}
            onReady={handleEditorReady}
            minHeight="650px"
            options={{
              appearance: { theme: "light" },
            }}
          />
        </div>
      </div>

      <SavePreviewModal
        isOpen={isPreviewModalOpen}
        title="Landing Page Preview"
        name={name}
        description={description}
        body={body}
        onConfirm={savePage}
        onCancel={() => setIsPreviewModalOpen(false)}
        confirmText="Save Landing Page"
      />


    </div>
  );
};

export default CreateLandingPage;
