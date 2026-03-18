import { useEffect, useState, useMemo } from "react";
import { 
  PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend 
} from "recharts";
import { Filter, Loader2 } from "lucide-react";
import { getLeads } from "../../api/apiLeads";
import type { Lead } from "../../types/lead";

const COLORS = {
  HOT: '#c21c1cff',  
  WARM: '#F59E0B', 
  COLD: '#2e6eddff'  
};

const LeadQualityWidget = () => {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLeads = async () => {
      try {
        const data = await getLeads();
        setLeads(data);
      } catch (error) {
        console.error("Error fetching leads:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchLeads();
  }, []);

  const chartData = useMemo(() => {
    let hotCount = 0;
    let warmCount = 0;
    let coldCount = 0;

    leads.forEach((lead) => {
      const score = lead.score || 0;

      if (score >= 80) {
        hotCount++;
      } else if (score >= 40) {
        warmCount++;
      } else {
        coldCount++;
      }
    });

    if (leads.length === 0) return [];

    return [
      { name: 'Hot (Sales Ready)', value: hotCount, color: COLORS.HOT },
      { name: 'Warm (Nurturing)', value: warmCount, color: COLORS.WARM },
      { name: 'Cold (Awareness)', value: coldCount, color: COLORS.COLD },
    ].filter(item => item.value > 0);
  }, [leads]);

  const totalLeads = leads.length;

  if (loading) {
    return (
      <div className="h-full w-full flex items-center justify-center">
        <Loader2 className="animate-spin text-blue-500" />
      </div>
    );
  }

  return (
    <div className="h-full w-full flex flex-col">
      <div className="flex justify-between items-center mb-2 px-1">
        <div>
          <h3 className="font-semibold text-gray-800">Lead Quality Score</h3>
          <p className="text-xs text-gray-500">Based on profile completeness</p>
        </div>
        <button className="text-gray-400 hover:text-gray-600">
          <Filter size={16} />
        </button>
      </div>

      <div className="flex-1 w-full min-h-0 relative">
        {totalLeads > 0 ? (
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={80}
                paddingAngle={5}
                dataKey="value"
                stroke="none"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip 
                contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                formatter={(value: number) => [`${value} Leads`, 'Count']}
              />
              <Legend 
                verticalAlign="bottom" 
                height={36} 
                iconType="circle" 
                iconSize={8}
                wrapperStyle={{fontSize: '11px', paddingTop: '10px'}}
              />
              
              <text x="50%" y="45%" textAnchor="middle" dominantBaseline="middle">
                <tspan x="50%" dy="0" fontSize="24" fontWeight="bold" fill="#1F2937">
                  {totalLeads}
                </tspan>
                <tspan x="50%" dy="20" fontSize="12" fill="#9CA3AF">
                  Total Leads
                </tspan>
              </text>
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-full flex flex-col items-center justify-center text-gray-400">
            <p className="text-sm">No leads found</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default LeadQualityWidget;