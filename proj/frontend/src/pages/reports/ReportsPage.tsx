import { useEffect, useState } from "react";
import { FileText } from "lucide-react";
import { getCampaigns, getCampaignReports } from "../../api/apiCampaigns";
import ReportCard from "../../components/cards/ReportCard";
import SearchAndFilterBar from "../../components/searchandfilters/SearchAndFilterBar";
import EmptyState from "../../components/states/EmptyState";
import LoadingState from "../../components/states/LoadingState";
import { useFlags } from "flagsmith/react";

const ReportsPage = () => {
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState<"All" | "Active" | "In Progress" | "Finished">("All");
  const [reportCards, setReportCards] = useState<
    {
      campaignId: number;
      campaignName: string;
      status: string;
      totalReports: number;
      lastGeneratedAt?: string;
    }[]
  >([]);

  const flags = useFlags(["enable_reports"]);
  const enableReports = flags.enable_reports.enabled;

  if (!enableReports) return null;

  useEffect(() => {
    const loadReports = async () => {
      try {
        const campaigns = await getCampaigns();

        const campaignReports = await Promise.all(
          campaigns.map(async (c) => {
            const reports = await getCampaignReports(c.id).catch(() => []);
            return {
              campaignId: c.id,
              campaignName: c.name,
              status: c.status,
              totalReports: reports.length,
              lastGeneratedAt: reports.length > 0 ? reports[reports.length - 1].generatedAt : undefined,
            };
          })
        );

        setReportCards(campaignReports);
      } catch (error) {
        console.error("Error retrieving reports:", error);
      } finally {
        setLoading(false);
      }
    };

    loadReports();
  }, []);

  const filtered = reportCards.filter((r) => {
    const matchesFilter = filter === "All" || r.status === filter;
    const matchesSearch = r.campaignName.toLowerCase().includes(search.toLowerCase());
    return matchesFilter && matchesSearch;
  });

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col">
      {/* HEADER */}
      <div className="bg-white shadow px-8 py-6 mx-8 mt-4 rounded-2xl">
        <h1 className="text-[22px] font-semibold">Reports</h1>
        <p className="text-gray-500 text-sm">Browse reports for each marketing campaign</p>
      </div>

      {/* CONTENT */}
      <div className="flex-1 p-8">
        <SearchAndFilterBar
          search={search}
          onSearchChange={setSearch}
          filter={filter}
          onFilterChange={setFilter}
          placeholder="Search campaigns..."
        />

        {loading ? (
          <LoadingState message="Loading reports..." />
        ) : filtered.length === 0 ? (
          <div className="bg-white border rounded-xl py-20 flex justify-center">
            <EmptyState
              icon={<FileText size={48} />}
              title="No reports found"
              description="No campaigns with available reports."
            />
          </div>
        ) : (
          <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
            {filtered.map((card) => (
              <ReportCard key={card.campaignId} reportInfo={card} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ReportsPage;
