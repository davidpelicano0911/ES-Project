import { useEffect, useState } from "react";
import { getMaterialsByCampaign } from "../../api/apiMaterials";
import type { EmailMaterial } from "../../api/apiMaterials";

interface EmailNodePropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: number | undefined | string) => void;
  campaignId?: number;
}

const EmailNodeProperties = ({
  selectedNode,
  onUpdateNode,
  campaignId,
}: EmailNodePropertiesProps) => {
  const data = selectedNode.data || {};
  const [emailMaterials, setEmailMaterials] = useState<EmailMaterial[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEmailMaterials = async () => {
      if (!campaignId) {
        setLoading(false);
        return;
      }
      
      try {
        const allMaterials = await getMaterialsByCampaign(campaignId);
        // Filter only EMAIL type materials
        const emailMaterials = allMaterials.filter(
          (material): material is EmailMaterial => material.type === "EMAIL"
        );
        setEmailMaterials(emailMaterials);
        console.log("Email materials fetched:", emailMaterials);
      } catch (err) {
        console.error("Error fetching email materials:", err);
      } finally {
        setLoading(false);
      }
    };

    

    fetchEmailMaterials();
  }, [campaignId]);

  useEffect(() => {
    // Set default sendFrom if not already set
    if (!data.sendFrom) {
      onUpdateNode("sendFrom", "marketingoperimus@gmail.com");
    }
  }, [data.sendFrom, onUpdateNode]);

  return (
    <div className="flex flex-col h-full w-full p-5 bg-white">
      <div className="flex-1 overflow-y-auto space-y-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">
          📧 {data.label || "Email Action"}
        </h2>

        <div>
          <label className="block text-xs text-gray-500 mb-1">Name</label>
          <input
            type="text"
            value={data.label || ""}
            onChange={(e) => onUpdateNode("label", e.target.value)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            placeholder="Enter email action name"
          />
        </div>

        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Email Template <span className="text-red-500">*</span>
          </label>
          <select
            value={data.emailTemplateId || ""}
            onChange={(e) => onUpdateNode("emailTemplateId", e.target.value ? parseInt(e.target.value, 10) : undefined)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            disabled={loading}
          >
            <option value="">
              {loading ? "Loading materials..." : campaignId ? "Select email material" : "No campaign context available"}
            </option>
            {emailMaterials.map((material) => (
              <option key={material.id} value={material.id.toString()}>
                {material.name}
              </option>
            ))}
          </select>
          {!loading && emailMaterials.length === 0 && (
            <p className="text-xs text-gray-400 mt-1">
              {campaignId ? "No email materials available for this campaign" : "Materials available only in campaign workflow context"}
            </p>
          )}
        </div>

        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Send From <span className="text-red-500">*</span>
          </label>
          <input
            type="email"
            value={data.sendFrom || "marketingoperimus@gmail.com"}
            onChange={(e) => onUpdateNode("sendFrom", e.target.value)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            placeholder="marketingoperimus@gmail.com"
          />
          <p className="text-xs text-gray-400 mt-1">
            Sender email address for this action
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

export default EmailNodeProperties;