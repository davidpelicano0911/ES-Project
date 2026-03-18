import { useNavigate } from "react-router-dom";
import type { FormTemplate } from "../../types/formTemplate";
import { Eye, Edit3, Trash2} from "lucide-react";
import { useUser } from "../../context/UserContext";
import RemoveFromCampaignButton from "../buttons/RemoveFromCampaignButton";

interface FormTemplateCardProps {
  template: FormTemplate;
  onDelete?: (template: FormTemplate) => void;
  onView: (template: FormTemplate) => void;
  onRemove?: (template: FormTemplate) => void;
  isInCampaign?: boolean;
}

const FormTemplateCard = ({ template, onDelete, onView, onRemove, isInCampaign = false }: FormTemplateCardProps) => {
  const navigate = useNavigate();
  const { hasRole } = useUser();

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDelete) {
      onDelete(template);
    }
  };

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onRemove) {
      onRemove(template);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[240px]">
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-lg font-semibold text-gray-900 leading-snug line-clamp-1">
            {template.name}
          </h3>

          {(!isInCampaign && hasRole("CONTENT_MARKETER")) && (
            <button
              onClick={handleDelete}
              className="p-1.5 hover:bg-red-50 rounded-md transition"
              title="Delete Form Template"
            >
              <Trash2 className="h-5 w-5 text-gray-500 hover:text-red-600 transition" />
            </button>
          )}
        </div>

        <p className="text-sm text-gray-600 mb-3 line-clamp-3">
          {template.description || "No description available."}
        </p>
      </div>

      <div className="flex border-t border-gray-200 divide-x divide-gray-200">
        {!isInCampaign && hasRole("CONTENT_MARKETER") && (
          <button
            onClick={() =>
              navigate(`/app/forms/builder/${template.id}`, { state: { template } })
            }
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
          className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-medium text-white 
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

export default FormTemplateCard;