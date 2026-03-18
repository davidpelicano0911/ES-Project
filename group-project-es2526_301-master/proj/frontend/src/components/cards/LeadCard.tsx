import { ArrowRight } from "lucide-react";
import type { Lead } from "../../types/lead";
import type { Segment } from "../../types/segment";
import { getSegments } from "../../api/apiSegments";
import { useEffect, useState } from "react";
import { useUser } from "../../context/UserContext";


interface LeadCardProps {
  lead: Lead;
  onView?: (id: number) => void;
}

const LeadCard = ({ lead, onView }: LeadCardProps) => {
  const [segments, setSegments] = useState<Segment[]>([]);
  const { hasRole } = useUser();


  useEffect(() => {
    const fetchSegments = async () => {
      try {
        const data = await getSegments();
        setSegments(data);
      } catch (err) {
        console.error("Error loading segments:", err);
      }
    };

    fetchSegments();
  }, []);

  const getStatusColor = (status?: string) => {
    switch (status) {
      case "Hot":
        return "text-red-600";
      case "Warm":
        return "text-orange-500";
      case "Cold":
        return "text-blue-600";
      default:
        return "text-gray-500";
    }
  };

  const derivedStatus = () => {
    if (lead.status) return lead.status;
    if (lead.score == null) return undefined;
    if (lead.score >= 67) return "Hot";
    if (lead.score >= 34) return "Warm";
    return "Cold";
  };

  return (
    <div className="bg-white rounded-2xl border border-[#E5E7EB] shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[200px] transition-all">
      <div className="p-6 flex-1">
        <h3 className="text-lg font-semibold text-[#111827] leading-snug">
          {lead.firstName} {lead.lastName}
        </h3>

        {lead.country && (
          <p className="text-sm text-[#6B7280] mt-1">{lead.country}</p>
        )}

        <p className="text-sm text-[#4B5563] mt-2">{lead.email}</p>

        {lead.phoneNumber && (
          <p className="text-sm text-[#4B5563] mt-1">{lead.phoneNumber}</p>
        )}

        {lead.score != null && (
          <p className="mt-3 text-sm font-medium text-gray-700">{lead.score} pts</p>
        )}

        {(lead.status || lead.score != null) && (
          <p
            className={`mt-3 text-sm font-medium ${getStatusColor(
              derivedStatus()
            )}`}
          >
            {derivedStatus()}
          </p>
        )}

        {lead.segmentIds && lead.segmentIds.length > 0 && (
          <div className="mt-3">
            {lead.segmentIds.map((segmentId) => {
              const id = Number(segmentId);
              const segment = segments.find((s) => s.id === id);

              return (
                <span
                  key={segmentId}
                  className="inline-block bg-[#E0E7FF] text-[#3730A3] text-xs font-medium mr-2 px-2.5 py-0.5 rounded"
                >
                  {segment ? segment.name : `Segment ${segmentId}`}
                </span>
              );
            })}
          </div>
        )}
      </div>

      {hasRole("SALES_REPRESENTATIVE") && (
        <button
          onClick={() => onView && onView(lead.id)}
          className="w-full bg-linear-to-r from-[#3B82F6] to-[#2563EB] hover:from-[#2563EB] hover:to-[#1D4ED8] text-white text-sm font-semibold py-3 flex items-center justify-center gap-2 rounded-b-2xl shadow-sm transition-all"
        >
          <span>View More</span>
          <ArrowRight className="h-4 w-4" />
        </button>
      )}
    </div>
  );
};

export default LeadCard;
