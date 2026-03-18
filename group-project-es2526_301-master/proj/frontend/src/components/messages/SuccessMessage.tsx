import { CheckCircle, X } from "lucide-react";

interface SuccessMessageProps {
  entity: string;
  onClose?: () => void;
}

const SuccessMessage = ({ entity, onClose }: SuccessMessageProps) => {
  return (
    <div className="fixed top-6 left-1/2 transform -translate-x-1/2 z-1000">
      <div className="flex items-center gap-2 bg-green-50 border border-green-300 text-green-800 rounded-lg px-4 py-3 shadow-md">
        <span className="text-green-600 text-lg">
          <CheckCircle className="h-5 w-5" />
        </span>
        <p className="text-sm font-medium">{entity} successfully!</p>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-3 text-green-700 hover:text-green-900 font-semibold"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default SuccessMessage;
