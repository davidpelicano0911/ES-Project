import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { SurveyCreatorComponent, SurveyCreator } from "survey-creator-react";
import "survey-core/survey-core.css";
import "survey-creator-core/survey-creator-core.css";
import BackButton from "../../components/buttons/BackButton";
import ConfirmationModal from "../../components/modals/ConfirmationModal";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import { Save } from "lucide-react";
import * as api from "../../api/apiFormBuilder";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";
import { useUser } from "../../context/UserContext";

export default function FormBuilderPage() {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const creatorRef = useRef<SurveyCreator | null>(null);

  const [creator, setCreator] = useState<SurveyCreator | null>(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [showSuccessMessage, setShowSuccessMessage] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formName, setFormName] = useState<string>(""
  );
  const [formDescription, setFormDescription] = useState<string>("");
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.FormsBuilder.Load", (span) => {
      span.setAttribute("page.name", "FormsBuilderPage");
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
    tracer.startActiveSpan("API.GetTemplate", (span) => {
      if (!creatorRef.current) {
        const newCreator = new SurveyCreator({ showLogicTab: true });
        creatorRef.current = newCreator;
        setCreator(newCreator);
      }

      const template = location.state?.template;

      // If editing an existing template, try to load its JSON from backend (fallback to state)
      if (id) {
        (async () => {
          try {
            const resp = await api.getTemplate(id);
            span.setAttribute("template.id", id);
            if (resp && resp.formJson) {
              // Parse if needed
              let parsedFormJson: any;
              try {
                parsedFormJson = typeof resp.formJson === "string"
                  ? JSON.parse(resp.formJson)
                  : resp.formJson;
                span.setAttribute("template.formJson", JSON.stringify(parsedFormJson));
                span.addEvent("template_formJson_parsed");
              } catch (err) {
                console.error("Failed to parse formJson:", err);
                span.recordException(err as Error);
                span.setStatus({ code: SpanStatusCode.ERROR, message: "Failed to parse formJson" });
                span.setAttribute("api.success", false);
                parsedFormJson = {}; // fallback
              }

              // Some saved forms wrap the real Survey JSON inside settings.surveyJson
              const surveyToLoad =
                parsedFormJson?.settings?.surveyJson ?? parsedFormJson;

              // Load into Survey Creator
              creatorRef.current!.JSON = surveyToLoad;

              // Set form metadata
              setFormName(location.state?.template?.name || resp.name || "");
              setFormDescription(location.state?.template?.description || resp.description || "");
              span.addEvent("template_loaded_into_creator");
              span.setAttribute("api.success", true);
            }
          } catch (e) {
            console.warn("Could not fetch template for edit", e);
            span.recordException(e as Error);
            span.setStatus({ code: SpanStatusCode.ERROR, message: "Failed to fetch template" });
            span.setAttribute("api.success", false);
          }
        })();
      }

      if (template && template.schema) {
        creatorRef.current!.JSON = template.schema;
        setFormName(template.name || "");
        setFormDescription(template.description || "");
      } else if (id === "1") {
        creatorRef.current!.JSON = {
          title: "Event Registration",
          elements: [
            { type: "text", name: "full_name", title: "Full Name" },
            { type: "text", name: "email", title: "Email Address" },
            { type: "text", name: "phone", title: "Phone Number" },
          ],
        };
      } else if (id === "2") {
        creatorRef.current!.JSON = {
          title: "Customer Feedback",
          elements: [
            { type: "rating", name: "satisfaction", title: "Satisfaction Level" },
            { type: "comment", name: "comments", title: "Additional Comments" },
          ],
        };
      } else {
        creatorRef.current!.JSON = { title: "New Form", pages: [] };
      }
      span.end();
    });
  }, [id, location.state]);

  const handleSaveClick = () => {
    tracer.startActiveSpan("User.ACTION.SaveForm", (span) => {
      span.addEvent("save_modal_opened");
      setIsConfirmModalOpen(true);
      span.end();
    });
  };

  const confirmSave = () => {
    tracer.startActiveSpan("API.SaveFormTemplate", (span) => {
      try {
        if (!creatorRef.current) return;
        // Use full SurveyJS JSON as the `form` payload
        const json = creatorRef.current.JSON;

        // Use the Form name input as the source of truth for the template name;
        // fall back to Survey JSON title or other state if the input is empty
        const name = formName || json?.title || (location.state?.template?.name as string) || "New Form";
        const description = formDescription || json?.description || (location.state?.template?.description as string) || "";

        // Convert SurveyJS JSON into the backend FormConfig shape
        const formConfig = convertSurveyToFormConfig(json);
        span.setAttribute("form.name", name);
        span.setAttribute("form.description", description);
        span.addEvent("form_converted_to_backend_config");

        const payload: api.FormTemplate = {
          name,
          description,
          createdBy: 1, // TODO: replace with real authenticated user ID from context
          formJson: JSON.stringify(formConfig),
        };

        // If editing (id present) call update, otherwise create
        const savePromise = id ? api.updateTemplate(id, payload as api.FormTemplate) : api.createTemplate(payload as api.FormTemplate);

        savePromise
          .then(() => {
            setIsConfirmModalOpen(false);
            span.addEvent("save_modal_closed");
            setShowSuccessMessage(true);
            span.addEvent("form_template_saved");
            span.setStatus({ code: SpanStatusCode.OK });
            span.setAttribute("api.success", true);
            setTimeout(() => {
              setShowSuccessMessage(false);
              navigate("/app/forms");
            }, 2000);
          })
          .catch((err) => {
            console.error("Failed to save template:", err);
            span.setAttribute("api.success", false);
            span.recordException(err as Error);
            span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
            setError("Failed to save form. Please try again.");
            setIsConfirmModalOpen(false);
            span.addEvent("save_modal_closed");
          });
      } catch {
        setError("Failed to save form. Please try again.");
        setIsConfirmModalOpen(false);
        span.addEvent("save_modal_closed");
        span.setAttribute("api.success", false);
        span.setStatus({ code: SpanStatusCode.ERROR, message: "Exception during save" });
        span.recordException(new Error("Exception during save"));
      }
      span.end();
    });
  };

  // Note: we store the full SurveyJS JSON in the backend `form` field.
  // Helper: convert SurveyJS JSON to FormConfig used by backend
  const convertSurveyToFormConfig = (surveyJson: any): any => {
    const components: api.FormComponent[] = [];

    const pages = surveyJson.pages || (surveyJson.pages === undefined && surveyJson.elements ? [{ elements: surveyJson.elements }] : []);

    (pages || []).forEach((page: any) => {
      const elems = page.elements || [];
      elems.forEach((el: any) => {
        const typeMap: Record<string, string> = {
          text: "textfield",
          comment: "textarea",
          dropdown: "select",
          checkbox: "checkbox",
          radiogroup: "radio",
          rating: "rating",
          boolean: "checkbox",
          matrix: "matrix",
          file: "file",
        };

        const compType = typeMap[el.type] || el.type || "textfield";
        const comp: api.FormComponent = {
          type: compType,
          key: el.name || el.id || `${compType}_${components.length}`,
          label: el.title || el.label || el.name || "",
          input: true,
          additionalProperties: undefined,
        };

        // map choices/options
        if (el.choices || el.items || el.options) {
          const options = el.choices || el.items || el.options;
          comp.additionalProperties = { options: options.map((o: any) => (typeof o === 'object' ? o.value ?? o.text ?? o : o)) };
        }

        components.push(comp);
      });
    });

    return {
      display: "form",
      // keep the full SurveyJS JSON in settings.surveyJson so we don't lose any metadata
      settings: { surveyJson },
      components,
    };
  };



  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      {showSuccessMessage && (
        <SuccessMessage
          entity="Form saved"
          onClose={() => setShowSuccessMessage(false)}
        />
      )}
      {error && <FailMessage entity={error} onClose={() => setError(null)} />}

      <BackButton to="/app/forms" label="Back to Templates" />

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mt-4 rounded-2xl flex justify-between items-center">
        <div className="flex flex-col">
          <h1 className="text-[22px] font-semibold text-[#111827]">Forms Builder</h1>
          <p className="text-[#6B7280] text-sm">
            Create or edit forms by dragging and dropping questions and fields below.
          </p>
          <div className="mt-3 flex gap-3 items-center">
            <input
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
              placeholder="Form name"
              className="border border-gray-200 rounded-lg px-3 py-2 text-sm w-[320px]"
            />
            <input
              value={formDescription}
              onChange={(e) => setFormDescription(e.target.value)}
              placeholder="Form description"
              className="border border-gray-200 rounded-lg px-3 py-2 text-sm w-[420px]"
            />
          </div>
        </div>

        <div className="flex gap-3">


          <button
            onClick={() => navigate("/app/forms")}
            className="border border-[#E5E7EB] text-[#374151] hover:bg-[#F9FAFB] px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm transition-all"
          >
            Cancel
          </button>

          <button
            onClick={handleSaveClick}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-4 py-2 rounded-md shadow transition"
          >
            <Save size={16} />
            Save Form
          </button>
        </div>

      </div>

      <div className="mt-8 bg-white rounded-xl shadow-sm border border-[#E5E7EB] p-6 w-full h-[85vh] overflow-hidden">
        <div className="h-full">
          {creator && <SurveyCreatorComponent creator={creator} />}
        </div>
      </div>



 
      {isConfirmModalOpen && (
        <ConfirmationModal
          title="Confirm Save"
          message="Are you sure you want to save this form? Changes will be applied to your templates."
          confirmText="Yes, Save"
          cancelText="Cancel"
          onConfirm={confirmSave}
          onCancel={() => setIsConfirmModalOpen(false)}
        />
      )}
    </div>
  );
}
