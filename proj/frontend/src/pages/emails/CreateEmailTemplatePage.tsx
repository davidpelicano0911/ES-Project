import { useState, useRef, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import EmailEditor from "react-email-editor";
import {
  createEmailTemplate,
  updateEmailTemplate,
  getEmailTemplateById,
} from "../../api/apiEmailTemplates";
import SavePreviewModal from "../../components/modals/SavePreviewModal";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import BackButton from "../../components/buttons/BackButton";
import LoadingState from "../../components/states/LoadingState";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

const CreateEmailTemplatePage = () => {
  const navigate = useNavigate();
  const { id } = useParams(); 
  const emailEditorRef = useRef<any>(null);

  const [existingTemplate, setExistingTemplate] = useState<any>(null);
  const [loading, setLoading] = useState(!!id);
  const [isPreviewModalOpen, setIsPreviewModalOpen] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [showFail, setShowFail] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [subject, setSubject] = useState("");
  const [body, setBody] = useState("");
  const { hasRole } = useUser();
  const tracer = useTracer();

  const projectId = import.meta.env.VITE_UNLAYER_PUBLIC_KEY || "";
  const requiredFilled = name.trim() !== "" && subject.trim() !== "";

  useEffect(() => {
    tracer.startActiveSpan("Page.CreateEmailTemplate.Load", (span) => {
      span.setAttribute("page.name", "CreateEmailTemplatePage");
      span.setAttribute("user.role.CONTENT_MARKETER", hasRole("CONTENT_MARKETER"));
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [hasRole, tracer]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetEmailTemplate", (span) => {
      if (!id) {
        localStorage.removeItem("emailDesign");
        span.end();
        return;
      }
      const fetchTemplate = async () => {
        try {
          setLoading(true);
          const data = await getEmailTemplateById(Number(id));
          setExistingTemplate(data);
          setName(data.name || "");
          setDescription(data.description || "");
          setSubject(data.subject || "");
          setBody(data.body || "");
          if (data.design) {
            localStorage.setItem("emailDesign", data.design);
          }
          span.setAttribute("api.success", true);
          span.addEvent("email_template_fetched");
          span.setStatus({ code: SpanStatusCode.OK });
        } catch (err) {
          console.error("Failed to load email template:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          setShowFail(true);
        } finally {
          setLoading(false);
          span.end();
        }
      };
    fetchTemplate();
    });
  }, [id]);


  const exportHtml = () => {
    if (!requiredFilled) return;
    if (!emailEditorRef.current?.editor) return;

    try {
      emailEditorRef.current.editor.exportHtml((data: any) => {
        if (!data?.html) {
          setBody("<p style='color:red'>Failed to export HTML.</p>");
          setIsPreviewModalOpen(true);
          return;
        }

        setBody(data.html);
        localStorage.setItem("emailDesign", JSON.stringify(data.design));
        setIsPreviewModalOpen(true);
      });
    } catch (err: any) {
      setBody(`<p style='color:red'>Failed to export HTML. ${err.message}</p>`);
      setIsPreviewModalOpen(true);
    }
  };

  const handleEditorReady = () => {
    if (!emailEditorRef.current?.editor) return;

    const storedDesign = localStorage.getItem("emailDesign");
    const designToLoad = storedDesign || existingTemplate?.design;

    if (designToLoad) {
      try {
        const parsedDesign =
          typeof designToLoad === "string"
            ? JSON.parse(designToLoad)
            : designToLoad;
        emailEditorRef.current.editor.loadDesign(parsedDesign);
      } catch (err) {
        console.warn("Invalid design format", err);
      }
    }
  };

  const saveTemplate = async (): Promise<void> => {
    tracer.startActiveSpan("API.SaveEmailTemplate", async (span) => {
      const design = localStorage.getItem("emailDesign") || existingTemplate?.design;
      const templatePayload = { name, subject, description, body, design };

      try {
        if (existingTemplate) {
          await updateEmailTemplate(existingTemplate.id, templatePayload);
        } else {
          await createEmailTemplate(templatePayload as any);
        }

        setShowSuccess(true);
        span.setAttribute("api.success", true);
        span.addEvent("email_template_saved");
        span.setStatus({ code: SpanStatusCode.OK });
        setTimeout(() => navigate("/app/email-templates"), 1500);
      } catch (err) {
        console.error("Failed to save email template:", err);
        setShowFail(true);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.addEvent("email_template_save_failed");
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        setTimeout(() => setShowFail(false), 3000);
      } finally {
        localStorage.removeItem("emailDesign");
        setIsPreviewModalOpen(false);
        span.end();
      }
    });
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#F9FAFB]">
        <LoadingState message="Loading email template..." />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {showSuccess && (
        <SuccessMessage
          entity={existingTemplate ? "Template updated" : "Template created"}
          onClose={() => setShowSuccess(false)}
        />
      )}
      {showFail && (
        <FailMessage
          entity="Template saving failed"
          onClose={() => setShowFail(false)}
        />
      )}

      <BackButton to="/app/email-templates" label="Back to Templates" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">
            {existingTemplate ? "Edit Email Template" : "Create Email Template"}
          </h1>
          <p className="text-[#6B7280] text-sm">
            Design and customize your marketing email template.
          </p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => navigate("/app/email-templates")}
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
            Preview Template
          </button>
        </div>
      </div>

      <div className="mt-8 bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-10 w-full">
        <div className="space-y-6 mb-6">
          <div>
            <label className="block text-sm font-medium text-[#111827] mb-1">
              Template Name <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Welcome Email Template"
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
              placeholder="Briefly describe this email template..."
              rows={3}
              className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 resize-none focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-[#111827] mb-1">
              Subject <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={subject}
              onChange={(e) => setSubject(e.target.value)}
              placeholder="Welcome to {CompanyName}, {FirstName}!"
              className="w-full border border-[#E5E7EB] rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#2563EB] placeholder:text-gray-400"
              required
            />
          </div>
        </div>

        <div className="h-[650px] border border-[#E5E7EB] rounded-lg overflow-hidden shadow-sm">
          <EmailEditor
            ref={emailEditorRef}
            projectId={projectId}
            onReady={handleEditorReady}
            minHeight="650px"
            options={{
              appearance: { theme: "light" },
              mergeTags: {
                FirstName: { name: "First Name", value: "{FirstName}" },
                LastName: { name: "Last Name", value: "{LastName}" },
                CompanyName: { name: "Company Name", value: "{CompanyName}" },
                Email: { name: "Email", value: "{Email}" },
              },
            }}
          />
        </div>
      </div>

      <SavePreviewModal
        isOpen={isPreviewModalOpen}
        name={name}
        subject={subject}
        body={body}
        fontSize={16}
        color="#111827"
        fontFamily="Inter"
        onCancel={() => setIsPreviewModalOpen(false)}
        onConfirm={() => void saveTemplate()}
      />
    </div>
  );
};

export default CreateEmailTemplatePage;
