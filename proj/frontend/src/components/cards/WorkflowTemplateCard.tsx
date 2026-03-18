import { useNavigate } from "react-router-dom";
import { Eye, Trash2 } from "lucide-react";
import type { WorkflowTemplate } from "../../types/workflow";
import { useUser } from "../../context/UserContext";
import { useFlags } from "flagsmith/react";

interface WorkflowTemplateCardProps {
  workflow: WorkflowTemplate;
  onDelete: (workflow: WorkflowTemplate) => void;
}

const WorkflowTemplateCard = ({
  workflow,
  onDelete,
}: WorkflowTemplateCardProps) => {
  const navigate = useNavigate();
  const { hasRole } = useUser();
  const { enable_workflow_template_deletion } = useFlags(["enable_workflow_template_deletion"]);
  const { enable_workflow_template_editing } = useFlags(["enable_workflow_template_editing"]);

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete(workflow);
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    navigate(`/app/workflow-templates/${workflow.id}`);
  };

  const formatDate = (isoString: string) => {
    const date = new Date(isoString);
    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[260px]">
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-lg font-semibold text-gray-900 leading-snug line-clamp-1">
            {workflow.name || "Untitled Workflow"}
          </h3>

          {enable_workflow_template_deletion.enabled && (hasRole("CONTENT_MARKETER") || hasRole("MARKETING_MANAGER")) && (
            <button
              onClick={handleDelete}
              className="p-1.5 hover:bg-red-50 rounded-md transition"
              title="Delete Workflow"
            >
              <Trash2 className="h-5 w-5 text-gray-500 hover:text-red-600 transition" />
            </button>
          )}
        </div>

        <p className="text-sm text-gray-600 mb-3 line-clamp-3">
          {workflow.description?.trim() || "No description available."}
        </p>

        <p className="text-xs text-gray-400">
          Created on{" "}
          <span className="font-medium text-gray-500">
            {formatDate(workflow.createdAt)}
          </span>
        </p>
      </div>

      <div className="flex border-t border-gray-200">
        <button
          onClick={handleEdit}
          className="flex-1 flex items-center justify-center gap-2 py-3 text-sm font-medium text-white 
            bg-linear-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 
            active:scale-[0.98] transition-all duration-150 rounded-b-2xl"
        >
          <Eye className="h-4 w-4" />
          {enable_workflow_template_editing.enabled && hasRole("MARKETING_MANAGER") ? "Preview and Edit" : "Preview"}
        </button>
      </div>
    </div>
  );
};

export default WorkflowTemplateCard;
