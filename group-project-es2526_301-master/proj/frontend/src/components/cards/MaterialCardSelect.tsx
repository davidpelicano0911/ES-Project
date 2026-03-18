import { Eye, Plus } from "lucide-react";
import { getTemplateIcon } from "../../utils/getTemplateIcon";
import type { Material } from "../../api/apiMaterials";

interface MaterialCardSelectProps {
  material: Material;
  onAdd: (materialId: number) => void;
  onView: (material: Material) => void;
}

const MaterialCardSelect = ({ material, onAdd, onView }: MaterialCardSelectProps) => {
  return (
    <div
      className="
        border border-[#E5E7EB] rounded-2xl shadow-sm 
        p-5 flex flex-col justify-between 
        hover:shadow-md transition 
        w-full 
        min-h-[250px] max-h-[320px]
      "
    >
      {/* TOP CONTENT */}
      <div className="flex-1">
        <div className="flex items-center gap-3 mb-3">
          <div className="p-2.5 bg-[#EEF2FF] rounded-xl">
            {getTemplateIcon(material.type || "EMAIL")}
          </div>
          <h3 className="text-[#111827] font-semibold text-base line-clamp-1">
            {material.name}
          </h3>
        </div>

        <p className="text-sm text-[#6B7280] line-clamp-3 mb-3">
          {material.description || "No description provided."}
        </p>

        <p className="text-xs text-[#9CA3AF]">
          Created on{" "}
          {new Date(material.createdAt || "").toLocaleDateString("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric",
          })}
        </p>
      </div>

      {/* FOOTER */}
      <div className="flex items-center justify-between mt-4 h-[48px]">
        <button
          onClick={() => onAdd(material.id)}
          className="
            flex-1 flex items-center justify-center gap-2 
            bg-[#2563EB] hover:bg-[#1D4ED8] text-white 
            text-sm font-medium 
            rounded-lg px-4 
            transition shadow-sm 
            h-full mr-2
          "
        >
          <Plus className="w-4 h-4" />
          Add
        </button>

        <button
          onClick={() => onView(material)}
          className="
            w-[48px] h-full flex items-center justify-center 
            border border-[#E5E7EB] bg-[#EEF2FF] hover:bg-[#E0E7FF] 
            rounded-lg transition shadow-sm
          "
          title="View Template"
        >
          <Eye className="w-5 h-5 text-[#4B5563]" />
        </button>
      </div>
    </div>
  );
};

export default MaterialCardSelect;
