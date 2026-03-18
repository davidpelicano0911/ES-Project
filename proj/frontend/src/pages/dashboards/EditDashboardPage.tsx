import { useState, useCallback, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Responsive, WidthProvider, type Layout } from "react-grid-layout";
import { Save, X, Search, Plus, LayoutTemplate, Activity, DollarSign, Users, MousePointer } from "lucide-react";
import BackButton from "../../components/buttons/BackButton";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import { updateDashboardLayout, getCampaignDashboard } from "../../api/apiDashboards";
import { getCampaignById } from "../../api/apiCampaigns";
import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";

import type { Post } from "../../types/post";

import KpiCard from "../../components/graphics/KpiCard";
import TrendChart from "../../components/graphics/TrendChart";
import FunnelChart from "../../components/graphics/FunnelChart";
import PostsTable from "../../components/graphics/PostsTable";
import SocialMetricsChart from "../../components/graphics/SocialMetricsChart";
import ContentPerformanceChart from "../../components/graphics/ContentPerformanceChart";
import LeadQualityWidget from "../../components/graphics/LeadQualityWidget";
import WorkflowStatusWidget from "../../components/graphics/WorkflowStatusWidget";
import PipelineWidget from "../../components/graphics/PipelineWidget";
import { getCampaignMaterials } from "../../api/apiCampaigns";  

const ResponsiveGridLayout = WidthProvider(Responsive);

type WidgetType = 
  | "kpi-conversion" 
  | "kpi-roi" 
  | "kpi-leads" 
  | "kpi-engagement" 
  | "chart-trend" 
  | "chart-funnel" 
  | "table-posts" 
  | "social-metrics" 
  | "post-card"
  | "content-performance" 
  | "chart-lead-quality" 
  | "list-workflows" 
  | "chart-pipeline";

interface WidgetItem extends Layout {
  type: WidgetType;
  postId?: number;
}

const EditDashboardPage = () => {
  const { id } = useParams();
  const [dashboardTitle, setDashboardTitle] = useState("");
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);
  const [availablePosts, setAvailablePosts] = useState<Post[]>([]);
  
  const [isSaving, setIsSaving] = useState(false);

  const [layout, setLayout] = useState<WidgetItem[]>([]);
  
  const [counter, setCounter] = useState(12);

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return;
      try {
        const [dash, campaign, materialsData] = await Promise.all([
          getCampaignDashboard(Number(id)).catch(() => null),
          getCampaignById(Number(id)).catch(() => null),
          getCampaignMaterials(Number(id)).catch(() => []) 
        ]);

        const postsOnly = (materialsData || []).filter((item: any) => 
            item.type === 'POST' || item.dtype === 'POST'
        );
        setAvailablePosts(postsOnly);
        
        if (dash && dash.title) setDashboardTitle(dash.title);
        else if (campaign && campaign.name) setDashboardTitle(campaign.name);

        if (dash && dash.layoutData) {
          const parsedLayout = JSON.parse(dash.layoutData);
          setLayout(parsedLayout);
          
          const maxId = parsedLayout.reduce((max: number, item: any) => {
             const num = parseInt(item.i.replace('widget-', '').replace('w-', '')) || 0;
             return num > max ? num : max;
          }, 0);
          setCounter(maxId + 1);
        }
      } catch (error) {
        console.log("Error loading data.", error);
      }
    };
  
    fetchData();
  }, [id]);

  const handleSaveLayout = async () => {
    if (!id) {
      setFailMessageVisible(true);
      return;
    }
    setIsSaving(true);

    try {
      const layoutString = JSON.stringify(layout);
      await updateDashboardLayout(Number(id), {
        title: dashboardTitle || "Campaign Dashboard", 
        layoutData: layoutString
      });
      setSuccessMessageVisible(true);
      setTimeout(() => setSuccessMessageVisible(false), 3000);
    } catch (error) {
      console.error("Error saving:", error);
      setFailMessageVisible(true);
      setTimeout(() => setFailMessageVisible(false), 3000);
    } finally {
      setIsSaving(false);
    }
  };

  const onLayoutChange = useCallback((newLayout: Layout[]) => {
    setLayout((currentLayout) => {
      return newLayout.map((newItem) => {
        const existingItem = currentLayout.find((old) => old.i === newItem.i);
        return {
          ...newItem,
          type: existingItem ? existingItem.type : 'kpi-conversion',
          postId: existingItem ? existingItem.postId : undefined 
        } as WidgetItem;
      });
    });
  }, []);

  const handleSelectPostForWidget = (widgetId: string, postIdString: string) => {
    const postId = Number(postIdString);
    setLayout((prevLayout) => 
      prevLayout.map(item => 
        item.i === widgetId ? { ...item, postId: postId } : item
      )
    );
  };

  const addWidget = (type: WidgetType) => {
    let w = 3, h = 4;

    if (type === 'chart-trend') { w = 8; h = 8; }
    if (type === 'table-posts') { w = 6; h = 7; }
    if (type === 'chart-funnel') { w = 6; h = 7; }
    if (type === 'social-metrics' || type === 'post-card') { w = 4; h = 6; }
    
    if (type === 'content-performance') { w = 4; h = 8; }
    if (type === 'chart-lead-quality') { w = 4; h = 8; }
    if (type === 'list-workflows') { w = 4; h = 8; }
    if (type === 'chart-pipeline') { w = 4; h = 8; }

    const newWidget: WidgetItem = {
      i: `widget-${counter}`,
      x: (layout.length * 2) % 12,
      y: Infinity,
      w, h,
      type,
    };
    
    setLayout([...layout, newWidget]);
    setCounter(counter + 1);
  };

  const removeWidget = (id: string) => {
    setLayout(layout.filter((item) => item.i !== id));
  };

  const renderWidgetContent = (widget: WidgetItem) => {
    switch (widget.type) {
      case "kpi-conversion":
        return <KpiCard title="Conversion Rate" value="4.8%" change="12%" isPositive={true} icon={<Activity />} colorClass="bg-purple-100 text-purple-600" />;
      case "kpi-roi":
        return <KpiCard title="ROI" value="340%" change="8%" isPositive={true} icon={<DollarSign />} colorClass="bg-green-100 text-green-600" />;
      case "kpi-leads":
        return <KpiCard title="Total Leads" value="2,847" change="24%" isPositive={true} icon={<Users />} colorClass="bg-blue-100 text-blue-600" />;
      case "kpi-engagement":
        return <KpiCard title="Engagement Rate" value="6.2%" change="-3%" isPositive={false} icon={<MousePointer />} colorClass="bg-orange-100 text-orange-600" />;
      
      case "chart-trend":
        return <TrendChart title="Campaign Trends" />;
      case "chart-funnel":
        return <FunnelChart />;
      case "table-posts":
        return <PostsTable campaignId={Number(id)} />;
      
      case "content-performance":
        return <ContentPerformanceChart />;
      case "chart-lead-quality":
        return <LeadQualityWidget />;
      case "list-workflows":
        return <WorkflowStatusWidget campaignId={Number(id)} />;
      case "chart-pipeline":
        return <PipelineWidget />;

      case "post-card":
      case "social-metrics":
        if (widget.postId) {
          const post = availablePosts.find(p => p.id === widget.postId);
          if (!post) return <div className="p-4 text-red-500 text-sm">Post not found (ID: {widget.postId})</div>;
          
          return (
            <div className="h-full w-full overflow-hidden relative">
               <button 
                 onClick={() => handleSelectPostForWidget(widget.i, "0")} 
                 className="absolute top-2 right-2 z-30 p-1 text-gray-400 hover:text-blue-500 bg-white/80 rounded-full border border-gray-100 shadow-sm"
                 title="Change Post"
               >
                 <Search size={14} />
               </button>
               <SocialMetricsChart post={post} />
            </div>
          );
        }
        return (
          <div className="flex flex-col items-center justify-center h-full p-4 text-center">
            <LayoutTemplate className="w-8 h-8 text-gray-400 mb-2 opacity-50" />
            <p className="text-sm font-medium text-gray-600 mb-2">Select a Post to Monitor</p>
            <select 
              className="w-full text-xs border-gray-300 rounded-md shadow-sm focus:border-blue-500 focus:ring-blue-500 border p-2 bg-white"
              onChange={(e) => handleSelectPostForWidget(widget.i, e.target.value)}
              defaultValue=""
            >
              <option value="" disabled>-- Choose a post --</option>
              {availablePosts.map(p => (
                <option key={p.id} value={p.id}>
                  {p.name || `Post #${p.id}`}
                </option>
              ))}
            </select>
          </div>
        );
      default:
        return <div>Widget Placeholder</div>;
    }
  };

  return (
    <div className="flex h-screen bg-[#F3F4F6] overflow-hidden relative">
      {successMessageVisible && <SuccessMessage entity="Layout saved" onClose={() => setSuccessMessageVisible(false)} />}
      {failMessageVisible && <FailMessage entity="Failed to save" onClose={() => setFailMessageVisible(false)} />}
      
      <aside className="w-72 bg-white border-r border-gray-200 flex flex-col shrink-0 z-10">
        <div className="p-4 border-b border-gray-100">
           <h2 className="font-bold text-gray-800 mb-4">Widget Library</h2>
           <div className="relative">
             <Search className="absolute left-3 top-2.5 text-gray-400" size={16} />
             <input type="text" placeholder="Search widgets..." className="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"/>
           </div>
        </div>

        <div className="flex-1 overflow-y-auto p-4 space-y-6">
          
          <div>
            <h3 className="text-xs font-semibold text-gray-500 uppercase mb-3 tracking-wider">Key Metrics (KPIs)</h3>
            <div className="space-y-2">
              <button onClick={() => addWidget("kpi-conversion")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">Conversion Rate <Plus size={14}/></button>
              <button onClick={() => addWidget("kpi-roi")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">ROI <Plus size={14}/></button>
              <button onClick={() => addWidget("kpi-leads")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">Total Leads <Plus size={14}/></button>
              <button onClick={() => addWidget("kpi-engagement")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">Engagement Rate <Plus size={14}/></button>
            </div>
          </div>

          <div>
            <h3 className="text-xs font-semibold text-gray-500 uppercase mb-3 tracking-wider">Automation & CRM</h3>
            <div className="space-y-2">
              <button onClick={() => addWidget("chart-lead-quality")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Lead Quality Score <Plus size={14}/>
              </button>
              <button onClick={() => addWidget("list-workflows")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Active Workflows <Plus size={14}/>
              </button>
              <button onClick={() => addWidget("chart-pipeline")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Pipeline Velocity <Plus size={14}/>
              </button>
            </div>
          </div>

          <div>
            <h3 className="text-xs font-semibold text-gray-500 uppercase mb-3 tracking-wider">Campaign Performance</h3>
            <div className="space-y-2">
              <button onClick={() => addWidget("chart-trend")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Trend Analysis <Plus size={14}/>
              </button>
              <button onClick={() => addWidget("content-performance")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Content Performance <Plus size={14}/>
              </button>
              <button onClick={() => addWidget("chart-funnel")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Conversion Funnel <Plus size={14}/>
              </button>
            </div>
          </div>
          
           <div>
            <h3 className="text-xs font-semibold text-gray-500 uppercase mb-3 tracking-wider">Social Media</h3>
            <div className="space-y-2">
              <button onClick={() => addWidget("table-posts")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Top Performing Posts <Plus size={14}/>
              </button>
              <button onClick={() => addWidget("post-card")} className="widget-btn-style w-full flex justify-between p-2 hover:bg-gray-50 rounded text-sm text-gray-700 hover:text-blue-600 transition-colors">
                Single Post Metrics <Plus size={14}/>
              </button>
            </div>
          </div>

        </div>
      </aside>

      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <BackButton to="/app/dashboard" label="Back to Dashboard" className="mx-8 mt-4"/>
        <header className="bg-white border-b border-gray-200 p-4 px-8 flex justify-between items-center shadow-sm z-10 mx-8 mt-4 rounded-2xl">
          <div>
            <h1 className="text-xl font-bold text-gray-800">
              Edit Campaign Dashboard – {dashboardTitle || "Loading..."}
            </h1>
            <p className="text-sm text-gray-500">Drag and drop widgets to customize your view</p>
          </div>
          <div className="flex items-center gap-3">
            <button 
              onClick={() => setLayout([])}
              className="px-4 py-2 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-md transition-colors"
            >
              Clear Layout
            </button>
            <button 
              onClick={handleSaveLayout}
              disabled={isSaving}
              className={`flex items-center px-4 py-2 rounded-md font-medium shadow-sm transition-colors ${
                isSaving ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'
              } text-white`}
            >
              <Save size={18} className="mr-2" /> 
              {isSaving ? "Saving..." : "Save Layout"}
            </button>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto p-8 bg-gray-50">
          <ResponsiveGridLayout
            className="layout"
            layouts={{ lg: layout }}
            breakpoints={{ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }}
            cols={{ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }}
            rowHeight={40}
            onLayoutChange={(currentLayout) => onLayoutChange(currentLayout)}
            draggableHandle=".drag-handle"
            margin={[20, 20]}
          >
            {layout.map((item) => (
              <div 
                key={item.i} 
                className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden flex flex-col hover:shadow-md transition-shadow group relative"
              >
                <div className="drag-handle absolute top-0 left-0 w-full h-5 cursor-move z-20 hover:bg-gray-100/80 opacity-0 group-hover:opacity-100 transition-opacity flex justify-center items-center">
                    <div className="w-8 h-1 bg-gray-300 rounded-full"></div>
                </div>

                <div className="absolute top-2 right-2 z-20 opacity-0 group-hover:opacity-100 transition-opacity">
                   <button 
                    onClick={() => removeWidget(item.i)}
                    className="p-1 text-gray-400 hover:text-red-500 bg-white/90 rounded-full shadow-sm border border-gray-100"
                    title="Remove Widget"
                  >
                    <X size={14} />
                  </button>
                </div>
                
                <div className="flex-1 p-4 overflow-hidden relative">
                  {renderWidgetContent(item)}
                </div>
              </div>
            ))}
          </ResponsiveGridLayout>
        </div>
      </main>
    </div>
  );
};

export default EditDashboardPage;