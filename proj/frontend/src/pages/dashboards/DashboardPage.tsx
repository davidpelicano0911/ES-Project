import { useEffect, useState } from "react";
import { Plus, LayoutDashboard } from "lucide-react";
import { useNavigate } from "react-router-dom";
import EmptyState from "../../components/states/EmptyState";
import DashboardCard from "../../components/cards/DashboardCard";
import { getCampaigns } from "../../api/apiCampaigns";
import type { Campaign } from "../../types/campaign";
import LoadingState from "../../components/states/LoadingState";
import SearchAndFilterBar from "../../components/searchandfilters/SearchAndFilterBar";
import { useUser } from "../../context/UserContext";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const DashboardPage = () => {
  const navigate = useNavigate();
  const { hasRole } = useUser();
  const [dashboards, setDashboards] = useState<
    {
      id: number;
      campaignId: number;
      title: string;
      campaignName: string;
      status: string;
      startDate: string;
      endDate: string;
    }[]
  >([]);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<
    "All" | "Active" | "In Progress" | "Finished"
  >("All");
  const [loading, setLoading] = useState(true);
  const tracer = useTracer();

  const statusMap: Record<string, string> = {
    ACTIVE: "Active",
    "IN PROGRESS": "In Progress",
    IN_PROGRESS: "In Progress",
    INPROGRESS: "In Progress",
    FINISHED: "Finished",
  };

  useEffect(() => {
    tracer.startActiveSpan("Page.Dashboard.Load", (span) => {
      span.setAttribute("page.name", "DashboardPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetDashboards", (span) => {
      const fetchDashboards = async () => {
        try {
          const campaigns: Campaign[] = await getCampaigns();
          const dashboardsFromApi = campaigns
            .filter((c) => c.dashboard)
            .map((c) => ({
              id: c.dashboard!.id,
              title: c.dashboard!.title,
              campaignName: c.name,
              campaignId: c.id,
              status: statusMap[c.status?.toUpperCase()] || c.status || "Unknown",
              startDate: new Date(c.createdAt).toLocaleDateString("en-GB"),
              endDate: new Date(c.dueDate).toLocaleDateString("en-GB"),
            }));
          span.setAttribute("api.success", true);
          span.addEvent("dashboards_fetched");
          span.setStatus({ code: SpanStatusCode.OK });
          setDashboards(dashboardsFromApi);
        } catch (error) {
          console.error("Error fetching dashboards:", error);
          span.setAttribute("api.success", false);
          span.recordException(error as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: String(error) });
        } finally {
          setLoading(false);
          span.end();
        }
      };
      fetchDashboards();
    });
  }, [tracer]);

  const filteredDashboards = dashboards.filter((d) => {
      const matchesFilter = filter === "All" || d.status === filter;
      
      const matchesSearch =
        d.title.toLowerCase().includes(search.toLowerCase()) ||
        d.campaignName.toLowerCase().includes(search.toLowerCase());
      
      return matchesFilter && matchesSearch;
  });

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col relative">
      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mx-8 mt-4 rounded-2xl flex justify-between items-center">
        <div>
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Dashboards
          </h1>
          <p className="text-[#6B7280] text-sm">
            Monitor and analyze your marketing campaigns
          </p>
        </div>
        {hasRole("MARKETING_ANALYST") && (
          <button
            onClick={() => navigate("/app/dashboard/create")}
            className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
          >
            <Plus className="h-4 w-4" />
            Create Dashboard
          </button>
        )}
      </div>

      <div className="flex-1 p-8">
        <SearchAndFilterBar
          search={search}
          onSearchChange={setSearch}
          filter={filter}
          onFilterChange={setFilter}
          placeholder="Search dashboards..."
        />

        {loading ? (
          <LoadingState message="Loading dashboards..." />
        ) : filteredDashboards.length === 0 ? (
          <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-20 flex justify-center items-center">
            <EmptyState
              icon={<LayoutDashboard size={48} />}
              title="No dashboards found"
              description="No dashboards were found. Try adjusting your search or create a new one."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filteredDashboards.map((dashboard) => (
              <DashboardCard key={dashboard.id} dashboard={dashboard} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
