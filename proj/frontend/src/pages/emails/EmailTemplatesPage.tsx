import { useState, useEffect } from "react";
import EmptyState from "../../components/states/EmptyState";
import { Mail, Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import ViewEmailTemplateModal from "../../components/modals/ViewEmailTemplateModal";
import LoadingState from "../../components/states/LoadingState";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import SearchBar from "../../components/searchandfilters/SearchBar";
import EmailTemplateCard from "../../components/cards/EmailTemplateCard";
import type { EmailTemplate } from "../../types/emailTemplate";
import {
  getEmailTemplates,
  deleteEmailTemplate,
} from "../../api/apiEmailTemplates";
import { useUser } from "../../context/UserContext";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const EmailTemplatesPage = () => {
  const [templates, setTemplates] = useState<EmailTemplate[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);
  const { hasRole } = useUser();
  const tracer = useTracer();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [templateToDelete, setTemplateToDelete] =
    useState<EmailTemplate | null>(null);

  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [templateToView, setTemplateToView] = useState<EmailTemplate | null>(
    null
  );

  const navigate = useNavigate();

  useEffect(() => {
    tracer.startActiveSpan("Page.EmailTemplates.Load", (span) => {
      span.setAttribute("page.name", "EmailTemplatesPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);      

  useEffect(() => {
    tracer.startActiveSpan("API.GetEmailTemplates", (span) => {
      const fetchTemplates = async () => {
        try {
          setLoading(true);
          const data = await getEmailTemplates();
          span.addEvent("email_templates_fetched");
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          setTemplates(data || []);
        } catch (err) {
          console.error("Failed to load email templates", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
          setError("Failed to fetch email templates");
        } finally {
          setLoading(false);
          span.end();
        }
      };
      fetchTemplates();
    });
  }, [tracer]);

  const filtered = templates.filter((t) =>
    tracer.startActiveSpan("User.ACTION.SearchEmailTemplates", (span) => {
      const matches = t.name.toLowerCase().includes(search.toLowerCase());
      span.setAttribute("search.query", search);
      span.setAttribute("search.result", matches);
      span.end();
      return matches;
    })
  );

  const handleDelete = (template: EmailTemplate) => {
    tracer.startActiveSpan("User.ACTION.DeleteEmailTemplate", (span) => {
      setTemplateToDelete(template);
      setIsDeleteModalOpen(true);
      span.addEvent("delete_modal_opened");
      span.end();
    });
  };

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeleteEmailTemplate", async (span) => {
      if (!templateToDelete) {
        span.end();
        return;
      }
      try {
        await deleteEmailTemplate(templateToDelete.id);
        setTemplates((prev) => prev.filter((t) => t.id !== templateToDelete.id));
        setIsDeleteModalOpen(false);
        span.addEvent("delete_modal_closed");
        setToastMessage({
          type: "success",
          text: `Template "${templateToDelete.name}" deleted`,
        });
        span.setAttribute("api.success", true);
        span.addEvent("email_template_deleted");
        span.setStatus({ code: SpanStatusCode.OK });
      } catch (err) {
        console.error("Failed to delete email template", err);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        setToastMessage({
          type: "fail",
          text: `Failed to delete "${templateToDelete.name}!"`,
        });
      } finally {
        setTimeout(() => setToastMessage(null), 3000);
        span.end();
      }
    });
  };

  const handleView = (template: EmailTemplate) => {
    tracer.startActiveSpan("User.ACTION.ViewEmailTemplate", (span) => {
      setTemplateToView(template);
      setIsViewModalOpen(true);
      span.addEvent("view_modal_opened");
      span.end();
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
            Email Templates
          </h1>
          <p className="text-[#6B7280] text-sm">
            Create and manage your email templates.
          </p>
        </div>

        {hasRole("CONTENT_MARKETER") && (
          <button
            onClick={() => navigate("/app/email-templates/create")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Email Template
          </button>
        )}
      </div>

      <div className="flex-1 p-8">
        <div className="mb-6 flex justify-start">
          <SearchBar
            value={search}
            onChange={setSearch}
            placeholder="Search email templates..."
          />
        </div>

        {loading ? (
          <LoadingState message="Loading email templates..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : filtered.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
            <EmptyState
              icon={<Mail size={48} />}
              title="No Email Templates Found"
              description="No email templates were found. Try adjusting your search or create a new one."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((t) => (
              <EmailTemplateCard
                key={t.id}
                template={t}
                onDelete={handleDelete}
                onView={handleView}
              />
            ))}
          </div>
        )}
      </div>

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete Email Template"
        entityName={templateToDelete?.name}
        message={`Are you sure you want to permanently delete "${
          templateToDelete?.name || "this template"
        }"? This action cannot be undone.`}
        confirmText="Delete Template"
        cancelText="Cancel"
        onConfirm={() => {
          void confirmDelete();
        }}
        onCancel={() => setIsDeleteModalOpen(false)}
      />

      {templateToView && (
        <ViewEmailTemplateModal
          isOpen={isViewModalOpen}
          id={templateToView.id}
          name={templateToView.name}
          subject={templateToView.subject}
          html={templateToView.body}
          onClose={() => setIsViewModalOpen(false)}
        />
      )}
    </div>
  );
};

export default EmailTemplatesPage;
