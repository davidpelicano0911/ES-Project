import { ArrowRight, FileText } from "lucide-react";
import { useNavigate } from "react-router-dom";

interface ReportCardProps {
  reportInfo: {
    campaignId: number;
    campaignName: string;
    status: string;
    totalReports: number;
    lastGeneratedAt?: string;
  };
}

const ReportCard = ({ reportInfo }: ReportCardProps) => {
  const navigate = useNavigate();

  const handleViewReports = () => {
    navigate(`/app/reports/${reportInfo.campaignId}`);
  };

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

  const currentStatus = statusColors[reportInfo.status] || {
    bg: "bg-gray-100",
    text: "text-gray-700",
    dot: "bg-gray-400",
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-sm hover:shadow-lg flex flex-col transition-all duration-200">
      <div className="p-6 flex-1">
        <div className="flex items-center gap-3 mb-3">
          <FileText className="h-6 w-6 text-indigo-600" />
          <h3 className="text-lg font-semibold text-gray-900">
            {reportInfo.campaignName}
          </h3>
        </div>

        <p className="text-sm text-gray-600 mb-2">
          Total reports: <span className="font-medium">{reportInfo.totalReports}</span>
        </p>

        {reportInfo.lastGeneratedAt && (
          <p className="text-sm text-gray-500">
            Last generated:{" "}
            {new Date(reportInfo.lastGeneratedAt).toLocaleString()}
          </p>
        )}

        <div className="mt-4">
          <span
            className={`inline-flex items-center gap-2 text-xs font-medium px-2.5 py-1 rounded-full ${currentStatus.bg} ${currentStatus.text}`}
          >
            <span className={`h-2 w-2 rounded-full ${currentStatus.dot}`} />
            {reportInfo.status}
          </span>
        </div>
      </div>

      <button
        onClick={handleViewReports}
        className="w-full bg-gradient-to-r from-indigo-500 to-indigo-600 hover:from-indigo-600 hover:to-indigo-700 text-white text-sm font-semibold py-3 flex items-center justify-center gap-2 rounded-b-2xl shadow-sm transition-all"
      >
        View Reports
        <ArrowRight className="h-4 w-4" />
      </button>
    </div>
  );
};

export default ReportCard;
