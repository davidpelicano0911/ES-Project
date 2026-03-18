import { useEffect, useState } from "react";
import { getMaterialsByCampaign } from "../../api/apiMaterials";
import type { FormMaterial } from "../../api/apiMaterials";

interface FormNodePropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: number | undefined | string) => void;
  campaignId?: number;
}

const FormNodeProperties = ({
  selectedNode,
  onUpdateNode,
  campaignId,
}: FormNodePropertiesProps) => {
  const data = selectedNode.data || {};
  const [formMaterials, setFormMaterials] = useState<FormMaterial[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFormMaterials = async () => {
      if (!campaignId) {
        setLoading(false);
        return;
      }
      
      try {
        const allMaterials = await getMaterialsByCampaign(campaignId);
        // Filter only FORM type materials
        const formMaterials = allMaterials.filter(
          (material): material is FormMaterial => material.type === "FORM"
        );
        setFormMaterials(formMaterials);
        console.log("Form materials fetched:", formMaterials);
      } catch (err) {
        console.error("Error fetching form materials:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchFormMaterials();
  }, [campaignId]);

  return (
    <div className="flex flex-col h-full w-full p-5 bg-white">
      <div className="flex-1 overflow-y-auto space-y-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">
          📝 {data.label || "Form Trigger"}
        </h2>

        <div>
          <label className="block text-xs text-gray-500 mb-1">Name</label>
          <input
            type="text"
            value={data.label || ""}
            onChange={(e) => onUpdateNode("label", e.target.value)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            placeholder="Enter form trigger name"
          />
        </div>

        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Form Material <span className="text-red-500">*</span>
          </label>
          <select
            value={data.formId ? data.formId.toString() : ""}
            onChange={(e) => onUpdateNode("formId", e.target.value ? parseInt(e.target.value, 10) : undefined)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            disabled={loading}
          >
            <option value="">
              {loading ? "Loading materials..." : campaignId ? "Select form material" : "No campaign context available"}
            </option>
            {formMaterials.map((material) => (
              <option key={material.id} value={material.id.toString()}>
                {material.name}
              </option>
            ))}
          </select>
          {!loading && formMaterials.length === 0 && (
            <p className="text-xs text-gray-400 mt-1">
              {campaignId ? "No form materials available for this campaign" : "Materials available only in campaign workflow context"}
            </p>
          )}
        </div>

        <div className="pt-4 mt-4 border-t border-gray-100">
          <h3 className="text-sm font-medium text-gray-700 mb-2">
            Trigger Information
          </h3>
          <p className="text-xs text-gray-500 leading-relaxed">
            This trigger will activate when someone submits the selected form. 
            The workflow will continue to the next connected action.
          </p>
        </div>

        <div className="pt-2 border-t border-gray-100">
          <p className="text-xs text-gray-400">
            <span className="text-red-500">*</span> Required fields
          </p>
        </div>
      </div>
    </div>
  );
};

export default FormNodeProperties;