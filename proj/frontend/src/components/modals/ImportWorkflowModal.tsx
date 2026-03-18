import { useEffect, useState } from "react";
import type { WorkflowTemplate,Workflow } from "../../types/workflow";
import { X } from "lucide-react";
import WorkflowCardImport from "../cards/WorkflowCardImport";
import SearchBar from "../searchandfilters/SearchBar";
import { getWorkflowTemplates } from "../../api/apiWorkflowTemplates";
import { createWorkflowFromTemplate } from "../../api/apiWorkflows";
import { useNavigate } from "react-router-dom";


interface ImportWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  campaignId: number;
  onWorkflowImported: (workflow: Workflow) => void;
}

const ImportWorkflowModal = ({
  isOpen,
  onClose,
  onWorkflowImported,
  campaignId,
}: ImportWorkflowModalProps) => {
  const [templates, setTemplates] = useState<WorkflowTemplate[]>([]);
  const [query, setQuery] = useState("");
  const [, setSelectedWorkflow] = useState<WorkflowTemplate | null>(null);
  const navigate = useNavigate();
  console.log("Campaign ID in ImportWorkflowModal:", campaignId);

  useEffect(() => {
    const fetchTemplates = async () => {
      try {
        const templates = await getWorkflowTemplates();
        setTemplates(
          templates.map((t) => ({
            id: t.id,
            name: t.name,
            description: t.description,
            createdAt: t.createdAt,
            templateData: t.templateData,
          }))
        );
      } catch (err) {
        console.error("Error fetching templates:", err);
        alert("Failed to load workflow templates.");
      }
    };
    fetchTemplates();
  }, []);

  const q = query.trim().toLowerCase();
  const filtered = templates.filter(
    (w) =>
      w.name.toLowerCase().includes(q) ||
      w.description?.toLowerCase().includes(q)
  );

  const handleImport = async (template: WorkflowTemplate) => {
    try {
      const newWorkflow = await createWorkflowFromTemplate(template.id, campaignId);
      onWorkflowImported(newWorkflow);
      onClose();
      navigate(`/app/workflows/${campaignId}`);
    } catch (err) {
      console.error("Failed to create workflow from template:", err);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/10 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-5xl p-8 animate-fadeIn">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-lg font-semibold text-[#111827]">
            Import Workflow Template
          </h2>
          <button
            onClick={onClose}
            className="text-[#6B7280] hover:text-[#111827] text-lg"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="mb-6">
          <SearchBar
            value={query}
            onChange={setQuery}
            placeholder="Search workflow templates..."
          />
        </div>

        {filtered.length === 0 ? (
          <div className="border border-dashed border-[#E5E7EB] rounded-lg py-12 text-center text-sm text-[#6B7280]">
            No workflows match “{query}”.
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filtered.map((template) => (
              <WorkflowCardImport
                key={template.id}
                workflow={template}
                onImport={handleImport}
                onView={(wf) => setSelectedWorkflow(wf)}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ImportWorkflowModal;
