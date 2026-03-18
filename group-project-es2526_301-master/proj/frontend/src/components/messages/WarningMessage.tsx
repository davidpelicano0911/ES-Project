import { AlertTriangle, X } from "lucide-react";

interface WarningMessageProps {
  message: string;
  onClose?: () => void;
}

const WarningMessage = ({ message, onClose }: WarningMessageProps) => {
  return (
    <div className="fixed top-6 left-1/2 transform -translate-x-1/2 z-1000">
      <div className="flex items-center gap-2 bg-yellow-50 border border-yellow-300 text-yellow-800 rounded-lg px-4 py-3 shadow-md">
        <span className="text-yellow-600 text-lg">
          <AlertTriangle className="h-5 w-5" />
        </span>
        <p className="text-sm font-medium">{message}</p>
        {onClose && (
          <button
            onClick={onClose}
            className="ml-3 text-yellow-700 hover:text-yellow-900 font-semibold"
          >
            <X className="h-4 w-4" />
          </button>
        )}
      </div>
    </div>
  );
};

export default WarningMessage;
