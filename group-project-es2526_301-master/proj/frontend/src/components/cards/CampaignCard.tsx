import { useNavigate } from "react-router-dom";
import type { Campaign } from "../../types/campaign";
import { Trash2, ArrowRight } from "lucide-react";
import { useUser } from "../../context/UserContext";

interface CampaignCardProps {
  campaign: Campaign;
  onDelete?: (id: number, name: string) => void;
}

const CampaignCard = ({ campaign, onDelete }: CampaignCardProps) => {
  const navigate = useNavigate();
  const { hasRole } = useUser();
  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete) onDelete(campaign.id, campaign.name);
  };

  const statusLabel =
    campaign.status === "ACTIVE"
      ? "Active"
      : campaign.status === "IN_PROGRESS"
      ? "In Progress"
      : "Finished";

  const statusColors = {
    ACTIVE: {
      bg: "bg-green-50",
      text: "text-green-700",
      dot: "bg-green-500",
    },
    IN_PROGRESS: {
      bg: "bg-yellow-50",
      text: "text-yellow-700",
      dot: "bg-yellow-500",
    },
    FINISHED: {
      bg: "bg-gray-100",
      text: "text-gray-700",
      dot: "bg-gray-400",
    },
  }[campaign.status];

  return (
    <div className="bg-white rounded-2xl border border-[#E5E7EB] shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[260px]">
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start">
          <h3 className="text-lg font-semibold text-[#111827] leading-snug">
            {campaign.name}
          </h3>

          {hasRole("MARKETING_MANAGER") && (
            <button
              onClick={handleDelete}
              className="p-1.5 hover:bg-red-50 rounded-md transition"
              title="Delete Campaign"
            >
              <Trash2 className="h-5 w-5 text-gray-500 hover:text-red-600" />
            </button>
          )}
        </div>

        <p className="text-sm text-[#6B7280] leading-snug mt-2 mb-4 line-clamp-3">
          {campaign.description}
        </p>

        {campaign.dueDate && (
          <p className="text-sm text-[#4B5563] mb-2">
            <span className="font-medium text-[#374151]">Due:</span>{" "}
            {new Date(campaign.dueDate).toLocaleDateString("en-GB", {
              day: "2-digit",
              month: "short",
              year: "numeric",
            })}
          </p>
        )}

        <span
          className={`inline-flex items-center gap-2 text-xs font-medium px-2.5 py-1 rounded-full ${statusColors.bg} ${statusColors.text}`}
        >
          <span className={`h-2 w-2 rounded-full ${statusColors.dot}`} />
          {statusLabel}
        </span>
      </div>

      <button
        onClick={() => navigate(`/app/campaigns/${campaign.id}`)}
        className="w-full bg-linear-to-r from-[#3B82F6] to-[#2563EB] hover:from-[#2563EB] hover:to-[#1D4ED8] text-white text-sm font-semibold py-3 flex items-center justify-center gap-2 rounded-b-2xl shadow-sm transition-all"
      >
        <span>View Details</span>
        <ArrowRight className="h-4 w-4" />
      </button>
    </div>
  );
};

export default CampaignCard;
