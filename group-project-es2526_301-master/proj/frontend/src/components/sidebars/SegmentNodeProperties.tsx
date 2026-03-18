import { useEffect, useState } from "react";
import { getSegments } from "../../api/apiSegments";
import type { NodeType } from "../../types/workflow";

interface SegmentNodePropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: number | undefined | string) => void;
  nodeType: NodeType;
}

const SegmentNodeProperties = ({
  selectedNode,
  onUpdateNode,
  nodeType,
}: SegmentNodePropertiesProps) => {
  const data = selectedNode.data || {};
  const [segments, setSegments] = useState<{ id: number; name: string }[]>([]);
  const [loading, setLoading] = useState(true);

  const isAddToSegment = nodeType === "ADD_TO_SEGMENT";
  const actionTitle = isAddToSegment ? "Add to Segment" : "Remove from Segment";
  const actionIcon = isAddToSegment ? "➕" : "➖";
  const actionColor = isAddToSegment ? "text-green-600" : "text-red-600";

  useEffect(() => {
    const fetchSegments = async () => {
      try {
        const response = await getSegments();
        setSegments(response);
        console.log("Segments fetched:", response);
      } catch (err) {
        console.error("Error fetching segments:", err);
      } finally {
        setLoading(false);
      }
    };

    fetchSegments();
  }, []);

  return (
    <div className="flex flex-col h-full w-full p-5 bg-white">
      <div className="flex-1 overflow-y-auto space-y-4">
        <h2 className={`text-lg font-semibold ${actionColor} mb-4`}>
          {actionIcon} {data.label || actionTitle}
        </h2>

        <div>
          <label className="block text-xs text-gray-500 mb-1">Name</label>
          <input
            type="text"
            value={data.label || ""}
            onChange={(e) => onUpdateNode("label", e.target.value)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            placeholder={`Enter ${actionTitle.toLowerCase()} name`}
          />
        </div>

        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Target Segment <span className="text-red-500">*</span>
          </label>
          <select
            value={data.segmentId || ""}
            onChange={(e) => onUpdateNode("segmentId", e.target.value ? parseInt(e.target.value, 10) : undefined)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            disabled={loading}
          >
            <option value="">
              {loading ? "Loading segments..." : "Select segment"}
            </option>
            {segments.map((segment) => (
              <option key={segment.id} value={segment.id.toString()}>
                {segment.name}
              </option>
            ))}
          </select>
          {!loading && segments.length === 0 && (
            <p className="text-xs text-gray-400 mt-1">
              No segments available
            </p>
          )}
        </div>

        <div className="pt-4 mt-4 border-t border-gray-100">
          <h3 className="text-sm font-medium text-gray-700 mb-2">
            Action Information
          </h3>
          <p className="text-xs text-gray-500 leading-relaxed">
            {isAddToSegment
              ? "This action will add contacts to the selected segment when triggered."
              : "This action will remove contacts from the selected segment when triggered."}
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

export default SegmentNodeProperties;