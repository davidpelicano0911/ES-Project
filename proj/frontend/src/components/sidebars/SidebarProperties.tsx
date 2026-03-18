import EmailNodeProperties from "./EmailNodeProperties";
import FormNodeProperties from "./FormNodeProperties";
import SegmentNodeProperties from "./SegmentNodeProperties";
import DelayNodeProperties from "./DelayNodeProperties";
import GenericNodeProperties from "./GenericNodeProperties";
import type { NodeType } from "../../types/workflow";

interface SidebarPropertiesProps {
  selectedNode: any;
  onUpdateNode: (field: string, value: string | number | undefined) => void;
  campaignId?: number;
}

const SidebarProperties = ({
  selectedNode,
  onUpdateNode,
  campaignId,
}: SidebarPropertiesProps) => {
  if (!selectedNode) {
    return (
      <div className="w-80 bg-white border-l border-gray-200 flex items-center justify-center text-gray-500 text-sm mt-8">
        No block selected
      </div>
    );
  }

  const nodeType: NodeType = selectedNode.type || selectedNode.data?.nodeType;

  // Route to appropriate sidebar component based on node type
  switch (nodeType) {
    case "EMAIL":
      return (
        <EmailNodeProperties
          selectedNode={selectedNode}
          onUpdateNode={onUpdateNode}
          campaignId={campaignId}
        />
      );
    case "FORM":
      return (
        <FormNodeProperties
          selectedNode={selectedNode}
          onUpdateNode={onUpdateNode}
          campaignId={campaignId}
        />
      );
    case "ADD_TO_SEGMENT":
    case "REMOVE_FROM_SEGMENT":
      return (
        <SegmentNodeProperties
          selectedNode={selectedNode}
          onUpdateNode={onUpdateNode}
          nodeType={nodeType}
        />
      );
    case "DELAY":
      return (
        <DelayNodeProperties
          selectedNode={selectedNode}
          onUpdateNode={onUpdateNode}
        />
      );
    default:
      // For START, END, LANDING_PAGE nodes
      return (
        <GenericNodeProperties
          selectedNode={selectedNode}
          onUpdateNode={onUpdateNode}
        />
      );
  }
};

export default SidebarProperties;
