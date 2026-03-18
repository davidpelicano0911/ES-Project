import { useEffect, useState, useRef } from "react";
import { useParams } from "react-router-dom";
import { Responsive, WidthProvider, type Layout } from "react-grid-layout";
import { Activity, DollarSign, Users, MousePointer, LayoutTemplate, FileText, Loader2 } from "lucide-react";

import { toJpeg } from "html-to-image";
import jsPDF from "jspdf";
import axios from "axios";

import BackButton from "../../components/buttons/BackButton";
import KpiCard from "../../components/graphics/KpiCard";
import TrendChart from "../../components/graphics/TrendChart";
import FunnelChart from "../../components/graphics/FunnelChart";
import PostsTable from "../../components/graphics/PostsTable";
import SocialMetricsChart from "../../components/graphics/SocialMetricsChart";
import ContentPerformanceChart from "../../components/graphics/ContentPerformanceChart";
import LeadQualityWidget from "../../components/graphics/LeadQualityWidget";
import WorkflowStatusWidget from "../../components/graphics/WorkflowStatusWidget";
import PipelineWidget from "../../components/graphics/PipelineWidget";

import { getCampaignDashboard } from "../../api/apiDashboards";
import { getCampaignById, getCampaignMaterials, uploadCampaignReport } from "../../api/apiCampaigns"; 
import type { Post } from "../../types/post";

import "react-grid-layout/css/styles.css";
import "react-resizable/css/styles.css";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";

const ResponsiveGridLayout = WidthProvider(Responsive);

type WidgetType = 
  | "kpi-conversion" | "kpi-roi" | "kpi-leads" | "kpi-engagement" 
  | "chart-trend" | "chart-funnel" | "table-posts" | "social-metrics" 
  | "post-card" | "content-performance" | "chart-lead-quality" 
  | "list-workflows" | "chart-pipeline";

interface WidgetItem extends Layout {
  type: WidgetType;
  postId?: number;
}

const DashboardDetails = () => {
  const { id } = useParams();
  const [dashboardTitle, setDashboardTitle] = useState("");
  const [layout, setLayout] = useState<WidgetItem[]>([]);
  const [availablePosts, setAvailablePosts] = useState<Post[]>([]);
  const [isGenerating, setIsGenerating] = useState(false);
  const dashboardRef = useRef<HTMLDivElement>(null);
  const [successMessageVisible, setSuccessMessageVisible] = useState(false);
  const [failMessageVisible, setFailMessageVisible] = useState(false);


  useEffect(() => {
    const loadData = async () => {
      if (!id) return;
      try {
        const [dashboard, campaign, postsData] = await Promise.all([
          getCampaignDashboard(Number(id)).catch(() => null),
          getCampaignById(Number(id)).catch(() => null),
          getCampaignMaterials(Number(id)).catch(() => [])
        ]);
        const postsOnly = (postsData || []).filter((item: any) => 
            item.type === 'POST' || item.dtype === 'POST'
        );
        setAvailablePosts(postsOnly);

        if (dashboard?.title) {
          setDashboardTitle(dashboard.title);
        } else if (campaign?.name) {
          setDashboardTitle(campaign.name);
        }

        if (dashboard?.layoutData) {
          const parsed = JSON.parse(dashboard.layoutData);
          setLayout(parsed);
        }
      } catch (err) {
        console.error("Error loading dashboard details:", err);
      }
    };

    loadData();
  }, [id]);

  const handleGenerateReport = async () => {
    if (!dashboardRef.current || !id) return;
    setIsGenerating(true);

    try {
      await new Promise((resolve) => setTimeout(resolve, 500));

      const element = dashboardRef.current;
      const totalHeight = element.scrollHeight;
      const totalWidth = element.scrollWidth;

      const dataUrl = await toJpeg(element, { 
        cacheBust: true,
        backgroundColor: "#F3F4F6", 
        pixelRatio: 2, 
        quality: 0.6, 
        skipFonts: true,
        width: totalWidth,
        height: totalHeight,
        style: {
          overflow: 'visible',
          maxHeight: 'none',
          height: 'auto'
        }
      });
      
      const pdf = new jsPDF("l", "mm", "a4"); 
      const pdfWidth = pdf.internal.pageSize.getWidth();
      
      const imgProps = pdf.getImageProperties(dataUrl);
      const ratio = imgProps.width / imgProps.height;
      const finalHeight = pdfWidth / ratio;

      pdf.addImage(dataUrl, "JPEG", 0, 0, pdfWidth, finalHeight);

      const timestamp = new Date().toISOString().slice(0, 10);
      const fileName = `Report_${dashboardTitle.replace(/\s+/g, '_')}_${timestamp}.pdf`;

      const pdfBlob = pdf.output("blob");
      await uploadCampaignReport(Number(id), pdfBlob, fileName);

      setSuccessMessageVisible(true);
      setTimeout(() => setSuccessMessageVisible(false), 3000);

    } catch (error) {
      console.error("Error generating report:", error);
      setFailMessageVisible(true);
      setTimeout(() => setFailMessageVisible(false), 3000);
      if (axios.isAxiosError(error) && error.response?.status === 413) {
          alert("Error: The report is too large. Increase the limit on the server or reduce the quality.");
          setFailMessageVisible(true);
          setTimeout(() => setFailMessageVisible(false), 3000);
      } else {
          alert("Failed to generate report.");
          setFailMessageVisible(true);
          setTimeout(() => setFailMessageVisible(false), 3000);
      }
    } finally {
      setIsGenerating(false);
    }
  };

  const renderWidget = (widget: WidgetItem) => {
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
          
          if (!post) {
            return (
              <div className="flex items-center justify-center h-full text-red-400 text-sm">
                 Post unavailable
              </div>
            );
          }
          
          return (
            <div className="h-full w-full overflow-hidden">
               <SocialMetricsChart post={post} />
            </div>
          );
        }
        return (
          <div className="flex flex-col items-center justify-center h-full p-4 text-center text-gray-400">
            <LayoutTemplate className="w-8 h-8 mb-2 opacity-50" />
            <p className="text-sm">No post selected</p>
          </div>
        );

      default:
        return <div className="text-gray-400 text-sm p-4">Unknown widget type</div>;
    }
  };

  return (
    <div className="flex h-screen bg-[#F3F4F6] overflow-hidden font-sans">
      <main className="flex-1 flex flex-col min-w-0 overflow-hidden">

        {successMessageVisible && <SuccessMessage entity="Report generated" />}
        {failMessageVisible && <FailMessage entity="Report generation" />}
        
        <BackButton to="/app/dashboard" label="Back to Dashboard" className="mx-8 mt-4" />

        <header className="bg-white border-b border-gray-200 p-4 px-8 shadow-sm mx-8 mt-4 rounded-2xl flex justify-between items-center">
          <div>
            <h1 className="text-xl font-bold text-gray-800">
                Campaign Dashboard – {dashboardTitle || "Loading..."}
            </h1>
            <p className="text-sm text-gray-500">
                Real-time performance overview
            </p>
          </div>

          <button
            onClick={handleGenerateReport}
            disabled={isGenerating}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors text-white ${
                isGenerating ? 'bg-indigo-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700'
            }`}
          >
            {isGenerating ? <Loader2 className="animate-spin" size={18} /> : <FileText size={18} />}
            {isGenerating ? "Generating..." : "Generate Report"}
          </button>
        </header>

        <div className="flex-1 overflow-y-auto p-8" ref={dashboardRef}>
          <ResponsiveGridLayout
            className="layout"
            layouts={{ lg: layout }}
            breakpoints={{ lg: 1200, md: 996, sm: 768, xs: 480, xxs: 0 }}
            cols={{ lg: 12, md: 10, sm: 6, xs: 4, xxs: 2 }}
            rowHeight={40}
            margin={[24, 24]}
            isDraggable={false}
            isResizable={false}
          >
            {layout.map((widget) => (
              <div
                key={widget.i}
                className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden flex flex-col p-4 hover:shadow-md transition-shadow"
              >
                {renderWidget(widget)}
              </div>
            ))}
          </ResponsiveGridLayout>
        </div>
      </main>
    </div>
  );
};

export default DashboardDetails;