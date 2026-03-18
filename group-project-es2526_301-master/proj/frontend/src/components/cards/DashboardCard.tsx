import { useNavigate } from "react-router-dom";
import { Edit, ArrowRight, BarChart3 } from "lucide-react";
import { useUser } from "../../context/UserContext";
import { useFlags } from "flagsmith/react";

interface DashboardCardProps {
  dashboard: {
    id: number;
    campaignId: number;
    title: string;
    campaignName: string;
    status: string;
    startDate: string;
    endDate: string;
    hasData?: boolean;
  };
}

const DashboardCard = ({ dashboard }: DashboardCardProps) => {
  const navigate = useNavigate();
  const { hasRole } = useUser();

  const flags = useFlags(["enable_view_dashboard", "enable_edit_dashboard"]);
  const enableViewDashboard = flags.enable_view_dashboard.enabled;
  const enableEditDashboard = flags.enable_edit_dashboard.enabled;




  const statusColors: Record<string, { bg: string; text: string; dot: string }> = {
    Active: {
      bg: "bg-green-50",
      text: "text-green-700",
      dot: "bg-green-500",
    },
    "In Progress": {
      bg: "bg-yellow-50",
      text: "text-yellow-700",
      dot: "bg-yellow-500",
    },
    Finished: {
      bg: "bg-gray-100",
      text: "text-gray-700",
      dot: "bg-gray-400",
    },
  };

  const currentStatus = statusColors[dashboard.status] || {
    bg: "bg-gray-100",
    text: "text-gray-700",
    dot: "bg-gray-400",
  };

  const handleNavigate = () => {
    navigate(`/app/dashboard/edit/${dashboard.campaignId}`);
  };

  const handleViewDashboard = () => {
    navigate(`/app/dashboard/view/${dashboard.campaignId}`);
  };


  return (
    <div className="bg-white rounded-2xl border border-[#E5E7EB] shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[260px] transition-all duration-200">
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start">
          <h3 className="text-lg font-semibold text-[#111827] leading-snug line-clamp-2">
            {dashboard.title}
          </h3>

          {hasRole("MARKETING_ANALYST") && enableEditDashboard && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                handleNavigate();
              }}
              className="p-1.5 hover:bg-gray-100 rounded-md transition"
              title="Edit Dashboard"
            >
              <Edit className="h-5 w-5 text-gray-500 hover:text-[#111827]" />
            </button>
          )}
        </div>

        <p className="text-sm text-[#6B7280] mt-3 mb-1">
          Campaign: <span className="font-medium text-[#374151]">{dashboard.campaignName}</span>
        </p>

        <div className="flex flex-wrap items-center gap-2 mt-auto">
          <span
            className={`inline-flex items-center gap-2 text-xs font-medium px-2.5 py-1 rounded-full ${currentStatus.bg} ${currentStatus.text}`}
          >
            <span className={`h-2 w-2 rounded-full ${currentStatus.dot}`} />
            {dashboard.status}
          </span>

          {!dashboard.hasData && (
            <span className="inline-flex items-center gap-1 text-xs font-medium text-gray-500 bg-gray-50 border border-gray-200 px-2.5 py-1 rounded-full">
              <BarChart3 size={12} />
              No Data
            </span>
          )}
        </div>
      </div>
      
      {enableViewDashboard && (
        <button
          onClick={handleViewDashboard}
          className="w-full bg-linear-to-r from-[#3B82F6] to-[#2563EB] hover:from-[#2563EB] hover:to-[#1D4ED8] text-white text-sm font-semibold py-3 flex items-center justify-center gap-2 rounded-b-2xl shadow-sm transition-all cursor-pointer"
        >
          <span>View Dashboard</span>
          <ArrowRight className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};

export default DashboardCard;