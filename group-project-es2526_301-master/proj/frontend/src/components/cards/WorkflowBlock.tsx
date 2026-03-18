import React from "react";

interface WorkflowBlockProps {
  icon: React.ReactNode;
  label: string;
  description: string;
  onClick: () => void;
}

const WorkflowBlock = ({
  icon,
  label,
  description,
  onClick,
}: WorkflowBlockProps) => {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-3 bg-[#F9FAFB] border border-gray-200 hover:border-[#2563EB] rounded-lg px-4 py-3 text-left w-56 shadow-sm hover:shadow-md transition"
    >
      <div className="bg-white p-2 rounded-md shadow-sm flex items-center justify-center w-8 h-8">
        {icon}
      </div>
      <div>
        <p className="font-medium text-gray-800 text-sm">{label}</p>
        <p className="text-xs text-gray-500">{description}</p>
      </div>
    </button>
  );
};

export default WorkflowBlock;
