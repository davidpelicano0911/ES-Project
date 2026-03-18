import React from "react";

interface ChartCardProps {
  title: string;
  children: React.ReactNode;
}

const ChartCard = ({ title, children }: ChartCardProps) => {
  return (
    <div className="bg-[#F9FAFB] rounded-xl p-4 border border-[#E5E7EB]">
      <h3 className="text-sm font-semibold mb-3 text-[#374151]">{title}</h3>
      {children}
    </div>
  );
};

export default ChartCard;
