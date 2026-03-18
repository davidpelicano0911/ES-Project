import { Loader2 } from "lucide-react";

type LoadingStateProps = {
  message?: string;
};

const LoadingState = ({ message = "Loading..." }: LoadingStateProps) => {
  return (
    <div className="flex flex-col items-center justify-center h-64 text-gray-600">
      <Loader2 className="h-10 w-10 animate-spin text-[#2563EB] mb-3" />
      <p className="text-sm font-medium">{message}</p>
    </div>
  );
};

export default LoadingState;
