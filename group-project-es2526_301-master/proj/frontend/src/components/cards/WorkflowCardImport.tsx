import { Upload } from "lucide-react";
import type { WorkflowTemplate } from "../../types/workflow";

interface WorkflowCardImportProps {
  workflow: WorkflowTemplate;
  onImport: (workflow: WorkflowTemplate) => void;
  onView: (workflow: WorkflowTemplate) => void;
}

const WorkflowCardImport = ({
  workflow,
  onImport,
}: WorkflowCardImportProps) => {
  return (
    <div className="border border-[#E5E7EB] rounded-xl shadow-sm p-5 flex flex-col justify-between hover:shadow-md transition">
      <div>
        <h3 className="text-[#111827] font-medium mb-1 line-clamp-1">
          {workflow.name}
        </h3>
        <p className="text-sm text-[#6B7280] line-clamp-3">
          {workflow.description || "No description provided."}
        </p>
        <p className="text-xs text-[#9CA3AF] mt-2">
          Created on{" "}
          {new Date(workflow.createdAt).toLocaleDateString("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric",
          })}
        </p>
      </div>

      <div className="flex items-center justify-center mt-5">
        <button
          onClick={() => onImport(workflow)}
          className="flex items-center justify-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-16 py-2.5 rounded-lg transition shadow-sm"
        >
          <Upload className="w-4 h-4" />
          Import
        </button>
      </div>
    </div>
  );
};

export default WorkflowCardImport;
