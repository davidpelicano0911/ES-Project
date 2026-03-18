import React from "react";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";

interface KpiCardProps {
  title: string;
  value: string;
  change: string;
  isPositive?: boolean;
  icon?: React.ReactNode;
  colorClass?: string; 
}

const KpiCard = ({ 
  title, 
  value, 
  change, 
  isPositive, 
  icon, 
  colorClass = "bg-gray-50 text-gray-600" 
}: KpiCardProps) => {
  return (
    <div className="flex flex-col justify-between h-full w-full p-1">
      <div className="flex justify-between items-start">
        <span className="text-sm font-medium text-gray-500">{title}</span>
        {icon && (
          <div className={`p-1.5 rounded-md ${colorClass}`}>
            {React.cloneElement(icon as React.ReactElement<any>, { size: 16 })}
          </div>
        )}
      </div>
      
      <div className="mt-2">
        <h2 className="text-3xl font-bold text-gray-900 tracking-tight">{value}</h2>
        <div className="flex items-center mt-1 text-sm">
          <span className={`flex items-center font-bold ${
            isPositive === true ? 'text-green-600' : 
            isPositive === false ? 'text-red-600' : 'text-gray-500'
          }`}>
            {isPositive === true && <TrendingUp size={14} className="mr-1" />}
            {isPositive === false && <TrendingDown size={14} className="mr-1" />}
            {isPositive === undefined && <Minus size={14} className="mr-1" />}
            {change}
          </span>
          <span className="text-gray-400 ml-1.5 font-medium">vs last month</span>
        </div>
      </div>
    </div>
  );
};

export default KpiCard;