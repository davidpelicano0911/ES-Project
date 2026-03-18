import { useState, useEffect } from "react";
import EmptyState from "../../components/states/EmptyState";
import type { Campaign } from "../../types/campaign";
import CampaignCard from "../../components/cards/CampaignCard";
import { Megaphone, Plus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import LoadingState from "../../components/states/LoadingState";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import { getCampaigns, deleteCampaign } from "../../api/apiCampaigns";
import SearchAndFilterBar from "../../components/searchandfilters/SearchAndFilterBar";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import { useUser } from "../../context/UserContext";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const CampaignPage = () => {
  const [campaigns, setCampaigns] = useState<Campaign[]>([]);
  const [filter, setFilter] = useState<
    "All" | "Active" | "In Progress" | "Finished"
  >("All");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [campaignToDelete, setCampaignToDelete] = useState<Campaign | null>(
    null
  );
  const { hasRole } = useUser();
  const navigate = useNavigate();
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.Campaigns.Load", (span) => {
      span.setAttribute("page.name", "CampaignsPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetCampaigns", async (span) => {
      try {
        setLoading(true);
        const data = await getCampaigns();
        setCampaigns(data);
        span.setAttribute("api.success", true);
        span.addEvent("campaigns_fetched");
        span.setStatus({ code: SpanStatusCode.OK });
      } catch (err) {
        setError("Failed to fetch campaigns");
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
      } finally {
        setLoading(false);
        span.end();
      }
    });
  }, [tracer]);

  const statusMap: Record<string, string> = {
    ACTIVE: "Active",
    "IN PROGRESS": "In Progress",
    IN_PROGRESS: "In Progress",
    INPROGRESS: "In Progress",
    FINISHED: "Finished",
  };

  const filtered = campaigns.filter((c) => {
    const backendStatus = statusMap[c.status?.toUpperCase()] || c.status;
    const matchesFilter = filter === "All" || backendStatus === filter;
    const matchesSearch = c.name.toLowerCase().includes(search.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  const handleDelete = (id: number) => {
    tracer.startActiveSpan("User.ACTION.DeleteCampaign", async (span) => {
      const campaign = campaigns.find((c) => c.id === id);
      if (!campaign) {
        span.end();
        return;
      }

      span.setAttribute("campaign.id", id);
      span.addEvent("delete_initiated");
      setCampaignToDelete(campaign);
      setIsDeleteModalOpen(true);
      span.addEvent("delete_modal_opened");
      span.end(); 
    });
  };

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeleteCampaign", async (span) => {
      if (!campaignToDelete) {
        span.end();
        return;
      }
      span.setAttribute("campaign.id", campaignToDelete.id);
      try {
        await deleteCampaign(campaignToDelete.id);
        span.addEvent("campaign_deleted");
        setCampaigns((prev) => prev.filter((x) => x.id !== campaignToDelete.id));
        setIsDeleteModalOpen(false);
        span.addEvent("delete_modal_closed");
        setToastMessage({
          type: "success",
          text: `Campaign "${campaignToDelete.name}" deleted`,
        });
        span.setStatus({ code: SpanStatusCode.OK });
      } catch (err) {
        console.error("Error deleting campaign:", err);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        setToastMessage({
          type: "fail",
          text: `Failed to delete "${campaignToDelete.name}"!`,
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
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Campaigns
          </h1>
          <p className="text-[#6B7280] text-sm">
            Manage your automation workflows
          </p>
        </div>

        {hasRole("MARKETING_MANAGER") && (
          <button
            onClick={() => navigate("/app/campaigns/create")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Campaign
          </button>
        )}
      </div>

      <div className="flex-1 p-8">
        <SearchAndFilterBar
          search={search}
          onSearchChange={setSearch}
          filter={filter}
          onFilterChange={setFilter}
          placeholder="Search campaigns..."
        />

        {loading ? (
          <LoadingState message="Loading campaigns..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : filtered.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
            <EmptyState
              icon={<Megaphone size={48} />}
              title="No Campaigns found!"
              description="No campaigns were found. Try adjusting your search or filters, or create a new one."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((c) => (
              <CampaignCard key={c.id} campaign={c} onDelete={handleDelete} />
            ))}
          </div>
        )}
      </div>

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete Campaign"
        entityName={campaignToDelete?.name}
        message={`Are you sure you want to permanently delete "${
          campaignToDelete?.name || "this campaign"
        }"? This action cannot be undone.`}
        confirmText="Delete Campaign"
        cancelText="Cancel"
        onConfirm={() => void confirmDelete()}
        onCancel={() => setIsDeleteModalOpen(false)}
      />
    </div>
  );
};

export default CampaignPage;
