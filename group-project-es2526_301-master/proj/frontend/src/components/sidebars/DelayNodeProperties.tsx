import { useEffect } from "react";

interface DelayNodePropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: string | number) => void;
}

const DelayNodeProperties = ({
  selectedNode,
  onUpdateNode,
  
}: DelayNodePropertiesProps) => {
  const data = selectedNode.data || {};

  useEffect(() => {
    // Set default delay values if not already set
    if (data.delayDuration === undefined) {
      onUpdateNode("delayDuration", 1);
    }
    if (data.delayUnit === undefined) {
      onUpdateNode("delayUnit", "HOURS");
    }
  }, [data.delayDuration, data.delayUnit, onUpdateNode]);

  return (
    <div className="flex flex-col h-full w-full p-5 bg-white">
      <div className="flex-1 overflow-y-auto space-y-4">
        <h2 className="text-lg font-semibold text-amber-600 mb-4">
          ⏱️ Delay Node
        </h2>

        <div>
          <label className="block text-xs text-gray-500 mb-1">Name</label>
          <input
            type="text"
            value={data.label || ""}
            onChange={(e) => onUpdateNode("label", e.target.value)}
            className="w-full border border-gray-300 rounded-md px-2 py-1 text-sm"
            placeholder="Enter node name"
          />
        </div>

        <div>
          <label className="block text-xs text-gray-500 mb-1">
            Delay Duration
          </label>
          <div className="flex space-x-2">
            <input
              type="number"
              value={data.delayDuration !== undefined ? data.delayDuration : 1}
              onChange={(e) => onUpdateNode("delayDuration", parseInt(e.target.value, 10))}
              className="flex-1 border border-gray-300 rounded-md px-2 py-1 text-sm"
              min="1"
              placeholder="1"
            />
            <select
              value={data.delayUnit !== undefined ? data.delayUnit : "HOURS"}
              onChange={(e) => onUpdateNode("delayUnit", e.target.value)}
              className="border border-gray-300 rounded-md px-2 py-1 text-sm"
            >
              <option value="MINUTES">Minutes</option>
              <option value="HOURS">Hours</option>
              <option value="DAYS">Days</option>
              <option value="WEEKS">Weeks</option>
            </select>
          </div>
        </div>

        <div className="pt-4 mt-4 border-t border-gray-100">
          <h3 className="text-sm font-medium text-gray-700 mb-2">
            Delay Information
          </h3>
          <p className="text-xs text-gray-500 leading-relaxed">
            This node will pause the workflow execution for the specified duration before continuing to the next action. The workflow will automatically resume after the delay period.
          </p>
        </div>
      </div>
    </div>
  );
};

export default DelayNodeProperties;