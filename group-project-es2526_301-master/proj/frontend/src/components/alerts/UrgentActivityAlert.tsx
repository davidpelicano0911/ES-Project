import { AlertTriangle } from "lucide-react";

interface UrgentActivityAlertProps {
  title?: string;
  message: string;
  color?: "red" | "yellow" | "blue";
  className?: string;
}

const UrgentActivityAlert = ({
  title = "Urgent Activity!",
  message,
  color = "red",
  className = "",
}: UrgentActivityAlertProps) => {
  const colorStyles = {
    red: {
      border: "border-red-200",
      text: "text-red-700",
      icon: "text-red-500",
      bg: "bg-white",
    },
    yellow: {
      border: "border-yellow-200",
      text: "text-yellow-700",
      icon: "text-yellow-500",
      bg: "bg-white",
    },
    blue: {
      border: "border-blue-200",
      text: "text-blue-700",
      icon: "text-blue-500",
      bg: "bg-white",
    },
  }[color];

  return (
    <div
      className={`absolute top-6 right-10 ${colorStyles.bg} ${colorStyles.border} shadow-sm rounded-lg p-3 flex items-center gap-3 text-sm ${colorStyles.text} ${className}`}
    >
      <AlertTriangle className={`w-5 h-5 ${colorStyles.icon}`} />
      <div>
        <p className="font-medium">{title}</p>
        <p className="text-xs">{message}</p>
      </div>
    </div>
  );
};

export default UrgentActivityAlert;
