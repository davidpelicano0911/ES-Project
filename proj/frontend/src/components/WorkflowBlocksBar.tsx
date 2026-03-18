import { useState } from "react";
import { Mail, FileText, PlusCircleIcon, MinusCircleIcon, Clock } from "lucide-react";
import WorkflowBlock from "./cards/WorkflowBlock";

interface WorkflowBlocksBarProps {
  onAddNode: (label: string, color: string, nodeType: string, eventType?: string) => void;
}

const WorkflowBlocksBar = ({ onAddNode }: WorkflowBlocksBarProps) => {
  const [activeTab, setActiveTab] = useState<"TRIGGERS" | "ACTIONS">("TRIGGERS");

  const triggers = [
    {
      icon: <FileText className="text-cyan-600" size={18} />,
      label: "Form",
      description: "Trigger on form submission",
      onClick: () => onAddNode("Form Submission", "#06B6D4", "FORM", "FORM_SUBMITTED"),
    },
  ];

  const actions = [
    {
      icon: <Mail className="text-blue-600" size={18} />,
      label: "Email",
      description: "Send email to contacts",
      onClick: () => onAddNode("Send Email", "#2563EB", "EMAIL", "SEND_EMAIL"),
    },
    {
      icon: <PlusCircleIcon className="text-green-600" size={18} />,
      label: "Add to Segment",
      description: "Add lead to a segment",
      onClick: () => onAddNode("Add to Segment", "#16A34A", "ADD_TO_SEGMENT", "ADD_TO_SEGMENT"),
    },
    {
      icon: <MinusCircleIcon className="text-red-600" size={18} />,
      label: "Remove from Segment",
      description: "Remove lead from a segment",
      onClick: () => onAddNode("Remove from Segment", "#DC2626", "REMOVE_FROM_SEGMENT", "REMOVE_FROM_SEGMENT"),
    },
    {
      icon: <Clock className="text-amber-600" size={18} />,
      label: "Delay",
      description: "Wait for a specified time",
      onClick: () => onAddNode("Delay", "#F59E0B", "DELAY", "DELAY"),
    }
  
  ];

  const currentBlocks = activeTab === "TRIGGERS" ? triggers : actions;

  return (
    <div className="bg-white border-t border-gray-200 p-5 flex flex-col items-center shrink-0">
      {/* Tabs */}
      <div className="flex gap-6 mb-4">
        <button
          onClick={() => setActiveTab("TRIGGERS")}
          className={`text-sm font-semibold ${
            activeTab === "TRIGGERS"
              ? "text-blue-600 border-b-2 border-blue-600"
              : "text-gray-500 hover:text-gray-700"
          } pb-1`}
        >
          Triggers
        </button>
        <button
          onClick={() => setActiveTab("ACTIONS")}
          className={`text-sm font-semibold ${
            activeTab === "ACTIONS"
              ? "text-blue-600 border-b-2 border-blue-600"
              : "text-gray-500 hover:text-gray-700"
          } pb-1`}
        >
          Actions
        </button>
      </div>

      {/* Blocks */}
      <div className="flex justify-center flex-wrap gap-4">
        {currentBlocks.map((block, idx) => (
          <WorkflowBlock
            key={idx}
            icon={block.icon}
            label={block.label}
            description={block.description}
            onClick={block.onClick}
          />
        ))}
      </div>
    </div>
  );
};

export default WorkflowBlocksBar;
