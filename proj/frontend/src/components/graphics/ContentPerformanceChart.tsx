import { useState, useEffect } from "react";
import { 
  BarChart, Bar, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend 
} from "recharts";
import { Mail, FileText, Globe } from "lucide-react";

import { getFormPerformanceStats } from "../../api/apiDashboards"; 

const dataEmail = [
  { name: 'Newsletter', open: 45, click: 12 },
  { name: 'Promo', open: 30, click: 5 },
  { name: 'Welcome', open: 65, click: 25 },
];


const dataPages = [
  { name: 'Pricing', visitors: 2000, conv: 2.5 },
  { name: 'Home', visitors: 5000, conv: 1.2 },
];

const ContentPerformanceChart = () => {
  const [activeTab, setActiveTab] = useState<'email' | 'forms' | 'pages'>('email');
  
  const [formData, setFormData] = useState<any[]>([]);
  const [, setLoading] = useState(false);

  useEffect(() => {
    if (activeTab === 'forms') {
      setLoading(true);
      getFormPerformanceStats()
        .then((data) => {
          setFormData(data);
        })
        .catch((err) => {
          console.error("Erro ao carregar stats dos forms:", err);
          setFormData([]); 
        })
        .finally(() => {
          setLoading(false);
        });
    }
  }, [activeTab]);

  return (
    <div className="h-full flex flex-col w-full">
      <div className="flex justify-between items-center mb-4 px-1">
        <h3 className="font-semibold text-gray-800">Content Performance</h3>
        
        <div className="flex bg-gray-100 p-1 rounded-lg">
          <button 
            onClick={() => setActiveTab('email')}
            className={`p-1.5 rounded transition-all ${activeTab === 'email' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-400 hover:text-gray-600'}`}
            title="Email Campaigns"
          >
            <Mail size={14} />
          </button>
          <button 
            onClick={() => setActiveTab('forms')}
            className={`p-1.5 rounded transition-all ${activeTab === 'forms' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-400 hover:text-gray-600'}`}
            title="Forms"
          >
            <FileText size={14} />
          </button>
          <button 
            onClick={() => setActiveTab('pages')}
            className={`p-1.5 rounded transition-all ${activeTab === 'pages' ? 'bg-white text-blue-600 shadow-sm' : 'text-gray-400 hover:text-gray-600'}`}
            title="Landing Pages"
          >
            <Globe size={14} />
          </button>
        </div>
      </div>

      <div className="flex-1 w-full min-h-0">
        <ResponsiveContainer width="100%" height="100%">
          {activeTab === 'email' ? (
            <BarChart data={dataEmail} barSize={20}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F3F4F6"/>
              <XAxis dataKey="name" tick={{fontSize: 11}} axisLine={false} tickLine={false} />
              <Tooltip cursor={{fill: '#F9FAFB'}} />
              <Legend iconSize={8} wrapperStyle={{ fontSize: '11px' }}/>
              <Bar name="Open Rate %" dataKey="open" fill="#8B5CF6" radius={[4, 4, 0, 0]} />
              <Bar name="Click Rate %" dataKey="click" fill="#C4B5FD" radius={[4, 4, 0, 0]} />
            </BarChart>
          ) : activeTab === 'forms' ? (
            <BarChart data={formData} layout="vertical" barSize={15}>
              <CartesianGrid strokeDasharray="3 3" horizontal={true} stroke="#F3F4F6"/>
              <XAxis type="number" hide />
              <YAxis dataKey="name" type="category" width={90} tick={{fontSize: 11}} axisLine={false} tickLine={false}/>
              <Tooltip cursor={{fill: '#F9FAFB'}} />
              <Legend iconSize={8} wrapperStyle={{ fontSize: '11px' }}/>
              
              
              <Bar name="Submissions" dataKey="subs" fill="#10B981" radius={[0, 4, 4, 0]} />
            </BarChart>
          ) : (
            <BarChart data={dataPages} barSize={25}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#F3F4F6"/>
              <XAxis dataKey="name" tick={{fontSize: 11}} axisLine={false} tickLine={false} />
              <Tooltip />
              <Bar name="Visitors" dataKey="visitors" fill="#3B82F6" radius={[4, 4, 0, 0]} />
            </BarChart>
          )}
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default ContentPerformanceChart;