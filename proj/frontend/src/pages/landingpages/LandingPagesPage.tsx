import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getLandingPages, deleteLandingPage } from "../../api/apiLandingPages";
import type { LandingPage } from "../../types/landingPage";
import SearchBar from "../../components/searchandfilters/SearchBar";
import EmptyState from "../../components/states/EmptyState";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import LandingPageCard from "../../components/cards/LandingPageCard";
import { Plus, Globe } from "lucide-react";
import SavePreviewModal from "../../components/modals/SavePreviewModal";
import { useUser } from "../../context/UserContext";
import LoadingState from "../../components/states/LoadingState";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const LandingPagesPage = () => {
  const [pages, setPages] = useState<LandingPage[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [pageToDelete, setPageToDelete] = useState<LandingPage | null>(null);

  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [pageToView, setPageToView] = useState<LandingPage | null>(null);

  const navigate = useNavigate();
  const { hasRole } = useUser();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.LandingPages.Load", (span) => {
      span.setAttribute("page.name", "LandingPagesPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetLandingPages", (span) => {
      const fetchPages = async () => {
        try {
          setLoading(true);
          const data = await getLandingPages();
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          span.addEvent("landing_pages_fetched");
          setPages(data || []);
        } catch (err) {
          console.error("Failed to load landing pages", err);
          setError("Failed to fetch landing pages");
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: "Failed to fetch landing pages" });
          span.addEvent("landing_pages_fetch_failed");
        } finally {
          setLoading(false);
          span.end();
        }
      };
      fetchPages();
    });
  }, [tracer]);

  const filtered = pages.filter((p) =>
    tracer.startActiveSpan("User.ACTION.FilterLandingPages", (span) => {
      const matches = p.name.toLowerCase().includes(search.toLowerCase());
      span.setAttribute("search.query", search);
      span.setAttribute("search.result", matches);
      span.end();
      return matches;
    })
  );

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeleteLandingPage", async (span) => {
      if (!pageToDelete?.id) {
        span.addEvent("no_page_to_delete");
        span.end();
        return; 
      }
      try {
        await deleteLandingPage(pageToDelete.id);
        setPages((prev) => prev.filter((p) => p.id !== pageToDelete.id));
        setIsDeleteModalOpen(false);
        span.addEvent("delete_modal_closed");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        span.addEvent("landing_page_deleted_successfully");
        setToastMessage({
          type: "success",
          text: `Landing page "${pageToDelete.name}" deleted`,
        });
      } catch (err) {
        console.error("Failed to delete landing page", err);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("landing_page_delete_failed");
        setToastMessage({
          type: "fail",
          text: `Failed to delete "${pageToDelete.name}"`,
        });
      } finally {
        setTimeout(() => setToastMessage(null), 3000);
        span.end();
      }
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
          <h1 className="text-[22px] font-semibold text-[#111827]">Landing Pages</h1>
          <p className="text-[#6B7280] text-sm">
            Create and manage your landing pages.
          </p>
        </div>

        {hasRole("CONTENT_MARKETER") && (
          <button
            onClick={() => navigate("/app/landing-pages/create")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Landing Page
          </button>
        )}

      </div>


      <div className="flex-1 p-8">
        <div className="mb-6 flex justify-start">
          <SearchBar
            value={search}
            onChange={setSearch}
            placeholder="Search landing pages..."
          />
        </div>

        {loading ? (
          <LoadingState message="Loading landing pages..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : filtered.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
            <EmptyState
              icon={<Globe size={48} />}
              title="No Landing Pages Found"
              description="Try creating a new landing page to start your campaign."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((page) => (
              <LandingPageCard
                key={page.id}
                page={page}
                onDelete={(p) => {
                  setPageToDelete(p);
                  setIsDeleteModalOpen(true);
                }}
                onView={(p) => {
                  setPageToView(p);
                  setIsViewModalOpen(true);
                }}
              />
            ))}
          </div>
        )}
      </div>


      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete Landing Page"
        entityName={pageToDelete?.name}
        message={`Are you sure you want to permanently delete "${
          pageToDelete?.name || "this landing page"
        }"? This action cannot be undone.`}
        confirmText="Delete"
        cancelText="Cancel"
        onConfirm={() => void confirmDelete()}
        onCancel={() => setIsDeleteModalOpen(false)}
      />

      <SavePreviewModal
        isOpen={isViewModalOpen}
        title="Landing Page Preview"
        name={pageToView?.name || ""}
        body={pageToView?.body || ""}
        confirmText="Close"
        onConfirm={() => setIsViewModalOpen(false)}
        onCancel={() => setIsViewModalOpen(false)}
        showCancel={false}
      />

    </div>
  );
};

export default LandingPagesPage;