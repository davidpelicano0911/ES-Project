interface KpiCardProps {
  label: string;
  value: string;
  delta: string;
}

const KpiCard = ({ label, value, delta }: KpiCardProps) => {
  return (
    <div className="bg-[#F9FAFB] rounded-xl p-4 border border-[#E5E7EB] text-center">
      <p className="text-sm text-[#6B7280]">{label}</p>
      <p className="text-xl font-semibold text-[#111827] mt-1">{value}</p>
      <p className="text-xs text-green-600 mt-1">{delta}</p>
    </div>
  );
};

export default KpiCard;
