import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend 
} from "recharts";
import { ArrowRightLeft } from "lucide-react";

const pipelineData = [
  { name: 'Week 1', mql: 40, sql: 12 },
  { name: 'Week 2', mql: 55, sql: 18 },
  { name: 'Week 3', mql: 35, sql: 25 },
  { name: 'Week 4', mql: 60, sql: 30 },
];

const PipelineWidget = () => {
  return (
    <div className="h-full w-full flex flex-col">
      <div className="flex justify-between items-center mb-2 px-1">
        <div>
          <h3 className="font-semibold text-gray-800">Pipeline Velocity</h3>
          <p className="text-xs text-gray-500">Marketing (MQL) vs Sales (SQL)</p>
        </div>
        <ArrowRightLeft size={16} className="text-gray-400"/>
      </div>

      <div className="flex-1 w-full min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart 
            data={pipelineData} 
            margin={{top: 10, right: 0, left: -25, bottom: 0}}
            barGap={2}
          >
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F3F4F6" />
            <XAxis 
              dataKey="name" 
              tick={{fontSize: 10, fill: '#6B7280'}} 
              axisLine={false} 
              tickLine={false} 
            />
            <YAxis 
              tick={{fontSize: 10, fill: '#6B7280'}} 
              axisLine={false} 
              tickLine={false} 
            />
            <Tooltip 
              cursor={{fill: '#F9FAFB'}}
              contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
            />
            <Legend 
              iconSize={8} 
              wrapperStyle={{fontSize: '11px', paddingTop: '10px'}} 
            />
            
            <Bar 
              dataKey="mql" 
              name="Marketing Qualified (MQL)" 
              fill="#93C5FD"
              radius={[4, 4, 0, 0]} 
              barSize={16} 
            />
            <Bar 
              dataKey="sql" 
              name="Sales Qualified (SQL)" 
              fill="#2563EB"
              radius={[4, 4, 0, 0]} 
              barSize={16} 
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default PipelineWidget;