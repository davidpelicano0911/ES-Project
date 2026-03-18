import type { NodeType } from "../../types/workflow";

interface GenericNodePropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: string) => void;
}

const GenericNodeProperties = ({
  selectedNode,
  onUpdateNode,
}: GenericNodePropertiesProps) => {
  const data = selectedNode.data || {};
  const nodeType: NodeType = selectedNode.type || selectedNode.data?.nodeType;

  const getNodeInfo = () => {
    switch (nodeType) {
      case "START":
        return {
          title: "🚀 Start Node",
          description: "This is the starting point of your workflow. All workflows begin here.",
          color: "text-green-600",
        };
      case "END":
        return {
          title: "🏁 End Node",
          description: "This marks the completion of your workflow. Contacts will exit the workflow here.",
          color: "text-gray-600",
        };
      case "DELAY":
        return {
          title: "⏱️ Delay Node",
          description: "This adds a time delay before continuing to the next action in the workflow.",
          color: "text-amber-600",
        };
      case "LANDING_PAGE":
        return {
          title: "🌐 Landing Page",
          description: "This represents a landing page interaction point in your workflow.",
          color: "text-pink-600",
        };
      default:
        return {
          title: "📋 Workflow Node",
          description: "Configure this workflow component.",
          color: "text-gray-600",
        };
    }
  };

  const nodeInfo = getNodeInfo();

  return (
    <div className="flex flex-col h-full w-full p-5 bg-white">
      <div className="flex-1 overflow-y-auto space-y-4">
        <h2 className={`text-lg font-semibold ${nodeInfo.color} mb-4`}>
          {data.label || nodeInfo.title}
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



        <div className="pt-4 mt-4 border-t border-gray-100">
          <h3 className="text-sm font-medium text-gray-700 mb-2">
            Node Information
          </h3>
          <p className="text-xs text-gray-500 leading-relaxed">
            {nodeInfo.description}
          </p>
        </div>

        {(nodeType === "START" || nodeType === "END") && (
          <div className="pt-2 border-t border-gray-100">
            <p className="text-xs text-gray-400">
              {nodeType === "START" 
                ? "This node cannot be deleted as it's required for workflow execution."
                : "This node marks the end of workflow execution for this path."
              }
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default GenericNodeProperties;