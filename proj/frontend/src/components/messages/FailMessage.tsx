import { XCircle, X } from "lucide-react";

interface FailMessageProps {
  entity: string;
  onClose?: () => void;
}

const FailMessage = ({ entity, onClose }: FailMessageProps) => {
  return (
    <div className="fixed top-6 left-1/2 transform -translate-x-1/2 z-1000">
      <div className="flex items-center gap-2 bg-red-50 border border-red-300 text-red-800 rounded-lg px-4 py-3 shadow-md">
        <span className="text-red-600 text-lg">
          <XCircle className="h-5 w-5" />
        </span>
        <p className="text-sm font-medium">{entity}</p>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-3 text-red-700 hover:text-red-900 font-semibold"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default FailMessage;
