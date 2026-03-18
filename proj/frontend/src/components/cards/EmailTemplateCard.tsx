import { useNavigate } from "react-router-dom";
import { Eye, Edit3, Trash2 } from "lucide-react";
import type { EmailTemplate } from "../../types/emailTemplate";
import { useUser } from "../../context/UserContext";
import RemoveFromCampaignButton from "../buttons/RemoveFromCampaignButton";

interface EmailTemplateCardProps {
  template: EmailTemplate;
  onDelete?: (template: EmailTemplate) => void;
  onView: (template: EmailTemplate) => void;
  onRemove?: (template: EmailTemplate) => void;
  isInCampaign?: boolean;
}

const EmailTemplateCard = ({
  template,
  onDelete,
  onView,
  onRemove,
  isInCampaign = false
}: EmailTemplateCardProps) => {
  const navigate = useNavigate();
  const { hasRole } = useUser();

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete) {
      onDelete(template);
    }
  };

  const formatDate = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };
  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onRemove) {
      onRemove(template);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[260px]">
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-lg font-semibold text-gray-900 leading-snug line-clamp-1">
            {template.name}
          </h3>

          {(!isInCampaign && hasRole("CONTENT_MARKETER")) && (
            <button
              onClick={handleDelete}
              className="p-1.5 hover:bg-red-50 rounded-md transition"
              title="Delete Template"
            >
              <Trash2 className="h-5 w-5 text-gray-500 hover:text-red-600 transition" />
            </button>
          )}
        </div>

        <p className="text-sm text-gray-600 mb-3 line-clamp-3">
          {template.description || "No description available."}
        </p>

        <p className="text-xs text-gray-400">
          Created on{" "}
          <span className="font-medium text-gray-500">
            {formatDate(template.createdAt)}
          </span>
        </p>
      </div>

      <div className="flex border-t border-gray-200 divide-x divide-gray-200 h-[48px]">
        {!isInCampaign && hasRole("CONTENT_MARKETER") && (
          <button
            onClick={() => navigate(`/app/email-templates/edit/${template.id}`)} 
            className="flex-1 flex items-center justify-center gap-2 py-3 text-sm font-medium text-gray-800 bg-gray-50 hover:bg-gray-100 active:scale-[0.98] transition-all duration-150 rounded-bl-2xl"
          >
            <Edit3 className="h-4 w-4" />
            Edit
          </button>
        )}

        {isInCampaign && onRemove && hasRole("MARKETING_MANAGER") && (
          <RemoveFromCampaignButton
            onClick={handleRemove}
            label="Remove"
            rounded={true}
          />
        )}


        <button
          onClick={() => onView(template)}
          className={`flex-1 h-full flex items-center justify-center gap-2 py-3 text-sm font-medium text-white 
    bg-linear-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 
    active:scale-[0.98] transition-all duration-150 rounded-br-2xl
    ${!hasRole("CONTENT_MARKETER") && !onRemove ? "rounded-bl-2xl" : ""}
    ${onRemove || hasRole("CONTENT_MARKETER") ? "" : "rounded-b-2xl"}`} 
        >
          <Eye className="h-4 w-4 text-white" />
          View
        </button>
      </div>
    </div>
  );
};

export default EmailTemplateCard;