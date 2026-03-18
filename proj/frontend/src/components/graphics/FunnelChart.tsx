import { 
  BarChart, Bar, Cell, XAxis, YAxis, ResponsiveContainer, Tooltip 
} from "recharts";
import { Filter } from "lucide-react";

const defaultFunnelData = [
  { name: 'Impressions', value: 12000, fill: '#E0E7FF' }, 
  { name: 'Clicks', value: 8500, fill: '#A5B4FC' },      
  { name: 'Leads', value: 3200, fill: '#6366F1' },       
  { name: 'Sales', value: 950, fill: '#4338CA' },      
];

const FunnelChart = () => {
  return (
    <div className="h-full flex flex-col w-full">
       <div className="flex justify-between items-center mb-2 px-1">
        <h3 className="font-semibold text-gray-800">Conversion Funnel</h3>
        <Filter size={16} className="text-gray-400" />
      </div>
      
      <div className="flex-1 w-full min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart layout="vertical" data={defaultFunnelData} barSize={24} margin={{ left: 10, right: 10 }}>
              <XAxis type="number" hide />
              <YAxis 
                dataKey="name" 
                type="category" 
                width={80} 
                tick={{fontSize: 12, fill: '#4B5563', fontWeight: 500}} 
                axisLine={false}
                tickLine={false}
              />
              <Tooltip 
                cursor={{fill: 'transparent'}}
                formatter={(value: number) => new Intl.NumberFormat('en-US').format(value)}
              />
              <Bar dataKey="value" radius={[0, 4, 4, 0]} background={{ fill: '#F9FAFB' }}>
                {defaultFunnelData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.fill} />
                ))}
              </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default FunnelChart;