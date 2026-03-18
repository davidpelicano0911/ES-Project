import { X } from "lucide-react";

interface RemoveFromCampaignButtonProps {
  onClick: (e: React.MouseEvent) => void;
  label?: string;
  rounded?: boolean;
}

const RemoveFromCampaignButton = ({
  onClick,
  label = "Remove",
  rounded = true,
}: RemoveFromCampaignButtonProps) => {
  return (
    <button
      onClick={onClick}
      className={`
        flex-1 h-full flex items-center justify-center gap-2 
        text-sm font-medium text-red-700 
        bg-red-50 hover:bg-red-100 
        active:scale-[0.98] transition 
        ${rounded ? "rounded-bl-2xl" : ""}
      `}
    >
      <X className="h-4 w-4 text-red-600" />
      {label}
    </button>
  );
};

export default RemoveFromCampaignButton;