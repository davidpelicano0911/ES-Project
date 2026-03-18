  import { useState, useEffect } from "react";
  import { useNavigate } from "react-router-dom";
  import SearchAndFilterBar from "../../components/searchandfilters/SearchAndFilterBar";
  import EmptyState from "../../components/states/EmptyState";
  import LoadingState from "../../components/states/LoadingState";
  import { User } from "lucide-react";
  import LeadCard from "../../components/cards/LeadCard";
  import type { Lead, LeadSyncStatusDTO } from "../../types/lead";
  import { getLeads, getLeadSyncStatus, syncLeads } from "../../api/apiLeads";
  import useTracer from "../../hooks/useTracer";
  import { SpanStatusCode } from "@opentelemetry/api";

  const LeadsPage = () => {
    const navigate = useNavigate();
    const [search, setSearch] = useState("");
    const [filter, setFilter] = useState<"All" | "Hot" | "Warm" | "Cold">("All");
    const [leads, setLeads] = useState<Lead[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [syncStatus, setSyncStatus] = useState<LeadSyncStatusDTO | null>(null);


    const tracer = useTracer();

    useEffect(() => {
      tracer.startActiveSpan("Page.Leads.Load", (span) => {
        span.setAttribute("page.name", "LeadsPage");
        span.addEvent("page_render_start");
        
        return () => {
          span.addEvent("page_render_complete");
          span.setStatus({ code: SpanStatusCode.OK });
          span.end();
        };
      });
    }, []);

    useEffect(() => {
      tracer.startActiveSpan("API.GetLeadSyncStatus", async (span) => {
        try {
          const data = await getLeadSyncStatus();
          setSyncStatus(data);
          span.setStatus({ code: SpanStatusCode.OK });
          span.addEvent("lead_sync_status_fetched");
          span.setAttribute("api.success", true);
          span.setAttribute("leads.updates_available", data.updatesAvailable);
          span.setAttribute("leads.pending_updates", data.pendingUpdates);
          span.setAttribute("leads.pending_creates", data.pendingCreates);
        } catch (err) {
          console.error(err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
          span.addEvent("lead_sync_status_fetch_failed");
        } finally {
          span.end();
        }
      });
    }, []);

    const handleSync = async () => {
      try {
        await syncLeads();

        const updated = await getLeadSyncStatus();
        setSyncStatus(updated);

        const refreshedLeads = await getLeads();
        setLeads(refreshedLeads);
      } catch (err) {
        console.error("Failed to sync leads", err);
      }
    };

    useEffect(() => {
      const fetchLeads = async () => {
        tracer.startActiveSpan("API.GetLeads", async (span) => {
          try {
            setLoading(true);
            const data = await getLeads();
            setLeads(data);
            span.setStatus({ code: SpanStatusCode.OK });
            span.addEvent("leads_fetched");
            span.setAttribute("api.success", true);
          } catch (err) {
            console.error(err);
            setError("Failed to fetch leads");
            span.setAttribute("api.success", false);
            span.recordException(err as Error);
            span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
            span.addEvent("leads_fetch_failed");
          } finally {
            setLoading(false);
            span.end();
          }
        });
      };

    fetchLeads();
  }, []);

  const filteredLeads = leads.filter((lead) => {
    const matchesSearch =
      `${lead.firstName} ${lead.lastName}`
        .toLowerCase()
        .includes(search.toLowerCase()) ||
      lead.email.toLowerCase().includes(search.toLowerCase()) ||
      (lead.country || "").toLowerCase().includes(search.toLowerCase());

    const matchesFilter = filter === "All" || lead.status === filter;

    return matchesSearch && matchesFilter;
  });

  const handleView = (id: number) => {
    navigate(`/app/leads/${id}`);
  };    


    return (
      <div className="min-h-screen bg-[#F9FAFB] flex flex-col relative">
        <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mx-8 mt-4 rounded-2xl flex justify-between items-center">
          <div>
            <h1 className="text-[22px] font-semibold text-[#111827]">Leads</h1>
            <p className="text-[#6B7280] text-sm">
              View and manage leads generated across your marketing campaigns
            </p>
          </div>
        </div>

        <div className="flex justify-between items-center px-8 mt-4">
          <div className="flex-1 py-2">
            <SearchAndFilterBar
              search={search}
              onSearchChange={setSearch}
              filter={filter}
              onFilterChange={setFilter}
              filters={["All", "Hot", "Warm", "Cold"]}
              placeholder="Search leads..."
            />
          </div>

          {syncStatus && (
            <div className="ml-6 px-4 py-2 rounded-lg border text-sm flex items-center gap-4
                            bg-white shadow-sm border-[#E5E7EB] max-w-md">

              <div className="flex flex-col">
                {syncStatus.updatesAvailable ? (
                  <span className="text-yellow-600 font-medium">
                    ⚠ Updates available — {syncStatus.pendingUpdates} updates,{" "}
                    {syncStatus.pendingCreates} new
                  </span>
                ) : (
                  <span className="text-green-600 font-medium">
                    ✓ Nothing to update
                  </span>
                )}

                {syncStatus.lastSyncedAt && (
                  <span className="text-gray-500 text-xs">
                    Last synced at {new Date(syncStatus.lastSyncedAt).toLocaleString()}
                  </span>
                )}
              </div>

              <button
                disabled={!syncStatus.updatesAvailable}
                onClick={handleSync}
                className={`px-3 py-1 rounded-lg text-sm font-medium transition
                  ${
                    syncStatus.updatesAvailable
                      ? "bg-blue-600 text-white hover:bg-blue-700"
                      : "bg-gray-200 text-gray-400 cursor-not-allowed"
                  }`}
              >
                Sync Now
              </button>
            </div>
          )}
        </div>

        <div className="flex-1 p-8">

          {loading ? (
            <LoadingState message="Loading leads..." />
          ) : error ? (
            <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
              <EmptyState
                icon={<User size={48} />}
                title="Failed to load leads"
                description="Please try again later."
              />
            </div>
          ) : filteredLeads.length === 0 ? (
            <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
              <EmptyState
                icon={<User size={48} />}
                title="No Leads found!"
                description="Try adjusting your filters or search terms."
              />
            </div>
          ) : (
            <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
              {filteredLeads.map((lead) => (
                <LeadCard key={lead.id} lead={lead} onView={handleView} />
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };

  export default LeadsPage;
