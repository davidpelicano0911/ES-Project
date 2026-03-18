import { useState, useEffect } from "react";
import SearchBar from "../../components/searchandfilters/SearchBar";
import EmptyState from "../../components/states/EmptyState";
import FormIcon from "../../assets/FormsLogo.svg";
import { useNavigate } from "react-router-dom";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import { useUser } from "../../context/UserContext";
import FormTemplateCard from "../../components/cards/FormTemplateCard";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import LoadingState from "../../components/states/LoadingState";
import ViewFormTemplateModal from "../../components/modals/ViewFormTemplateModal";
import type { FormTemplate } from "../../types/formTemplate";
import { useFormTemplates } from "../../hooks/useFormTemplates";
import { Plus } from "lucide-react";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const FormsPage = () => {
  const [search, setSearch] = useState("");
  const { templates: templateList, loading: hookLoading, error: hookError, deleteTemplate, getTemplate } = useFormTemplates();
  // convert backend template list items to the local FormTemplate shape expected by the UI
  const templates: FormTemplate[] = (templateList || []).map((t: any) => ({
    id: t.id,
    name: t.name,
    description: t.description,
    schema: (t as any).schema || undefined,
  }));
  const navigate = useNavigate();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [templateToDelete, setTemplateToDelete] = useState<FormTemplate | null>(null);
  const [showSuccessToast, setShowSuccessToast] = useState(false);

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const [selectedForm, setSelectedForm] = useState<FormTemplate | null>(null);
  const { hasRole } = useUser();
  const tracer = useTracer();
  
  useEffect(() => {
    tracer.startActiveSpan("Page.Forms.Load", (span) => {
      span.setAttribute("page.name", "FormsPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    // initialize loading state from hook
    setLoading(hookLoading);
    if (hookError) setError(hookError as unknown as string);
  }, [hookLoading, hookError]);

  const filtered = templates.filter((t) =>
    tracer.startActiveSpan("User.ACTION.FilterTemplates", (span) => {
      const matches = t.name.toLowerCase().includes(search.toLowerCase());
      span.setAttribute("search.query", search);
      span.setAttribute("search.result", matches);
      span.end();
      return matches;
    }),
  );

  const handleDeleteClick = (template: FormTemplate) => {
    tracer.startActiveSpan("User.ACTION.DeleteTemplate", (span) => {
      setTemplateToDelete(template);
      span.setAttribute("template.id", template.id);
      span.setAttribute("template.name", template.name);
      setIsDeleteModalOpen(true);
      span.addEvent("delete_modal_opened");
      span.end();
    });
  };

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeleteTemplate", async (span) => {
      if (!templateToDelete) {
        span.addEvent("no_template_to_delete");
        span.end();
        return;
      }

      try {
        await deleteTemplate(templateToDelete.id);
        setShowSuccessToast(true);
        span.addEvent("template_deleted_successfully");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        setTemplateToDelete(null);
        setIsDeleteModalOpen(false);
        span.addEvent("delete_modal_closed");
      } catch (err) {
        setError('Failed to delete template');
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("template_delete_failed");
        console.error('Delete failed', err);
      } finally {
        setTimeout(() => setShowSuccessToast(false), 2500);
        span.end();
      }
    });
  };

  const cancelDelete = () => {
    tracer.startActiveSpan("User.ACTION.CancelDeleteTemplate", (span) => {
      setTemplateToDelete(null);
      setIsDeleteModalOpen(false);
      span.addEvent("delete_modal_closed");
      span.end();
    });
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col relative">
      {showSuccessToast && (
        <SuccessMessage
          entity="Template deleted"
          onClose={() => setShowSuccessToast(false)}
        />
      )}

      {error && <FailMessage entity={error} onClose={() => setError(null)} />}

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mx-8 mt-4 rounded-2xl flex justify-between items-center">
        <div>
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Form Templates
          </h1>
          <p className="text-[#6B7280] text-sm">
            Create and manage your form templates.
          </p>
        </div>

        {hasRole("CONTENT_MARKETER") && (
          <button
            onClick={() => navigate("/app/forms/builder")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Template
          </button>
        )}
      </div>

      <div className="flex-1 p-8">
        <div className="mb-6 flex justify-start">
          <SearchBar
            value={search}
            onChange={setSearch}
            placeholder="Search form templates..."
          />
        </div>

        {loading ? (
          <LoadingState message="Loading form templates..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : filtered.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
            <EmptyState
              icon={<img src={FormIcon} alt="Form Templates" />}
              title="No Form Templates Found"
              description="Create your first form template to start collecting data."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 justify-start">
            {filtered.map((t) => (
              <FormTemplateCard
                key={t.id}
                template={t}
                onDelete={handleDeleteClick}
                onView={async (template) => {
                  try {
                    const preview = await getTemplate(template.id);
                    let parsedSchema = undefined;
                    try {
                      parsedSchema = typeof preview?.formJson === "string"
                        ? JSON.parse(preview.formJson)
                        : preview?.formJson;
                    } catch (err) {
                      console.error("Failed to parse formJson:", err);
                    }
                    console.log('Fetched template preview:', parsedSchema);
                    setSelectedForm({
                      id: template.id,
                      name: template.name,
                      description: template.description,
                      schema: parsedSchema.settings.surveyJson || undefined,
                    });
                  } catch (err) {
                    setError("Failed to load form preview");
                  }
                }}
              />
            ))}
          </div>
        )}
      </div>

      <ViewFormTemplateModal
        isOpen={!!selectedForm}
        form={selectedForm || undefined}
        onClose={() => setSelectedForm(null)}
      />


      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        entityName={templateToDelete?.name}
        onConfirm={confirmDelete}
        onCancel={cancelDelete}
      />
    </div>
  );
};

export default FormsPage;
