import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

interface BackButtonProps {
  to?: string;
  label?: string;
  className?: string;
  replace?: boolean;
  fallbackPath?: string;
}

const BackButton = ({
  to,
  label = "Back",
  className = "",
  replace = false,
  fallbackPath = "/app",
}: BackButtonProps) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (to) {
      navigate(to, { replace });
      return;
    }
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate(fallbackPath, { replace });
    }
  };

  return (
    <button
      onClick={handleBack}
      className={`flex items-center gap-2 text-[#374151] text-sm font-medium hover:text-[#2563EB] transition ${className}`}
    >
      <ArrowLeft className="w-4 h-4" />
      {label}
    </button>
  );
};

export default BackButton;
