import { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import { getPostStats } from "../../api/apiPosts";
import type { Post } from "../../types/post";
import { ThumbsUp, MessageCircle, Share2, Eye, Facebook, Calendar } from "lucide-react"; 
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

const SocialMetricsChart = ({ post }: { post: Post }) => {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  
  const isPublished = post.platforms.some(p => p.status === "PUBLISHED");

  useEffect(() => {
    if (!isPublished) return;

    setLoading(true);
    getPostStats(post.id)
      .then((data) => setStats(data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [post.id, isPublished]);

  const platformsWithScore = post.platforms.filter(p => p.performanceScore != null);
  const averageScore = platformsWithScore.length > 0
    ? (platformsWithScore.reduce((acc, curr) => acc + (curr.performanceScore || 0), 0) / platformsWithScore.length).toFixed(2)
    : "N/A";

  if (!isPublished) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-4 text-center text-gray-400">
        <Calendar className="mb-2 opacity-50" />
        <p className="text-sm font-medium">Post Scheduled</p>
        <span className="text-xs">Metrics available after publication.</span>
      </div>
    );
  }

  return (
    <div className="bg-white p-5 rounded-xl h-full flex flex-col relative">
      <div className="flex justify-between items-start mb-4">
        <div className="overflow-hidden">
          <h4 className="font-bold text-gray-800 text-lg truncate pr-2" title={post.name}>
            {post.name}
          </h4>
          
          <div className="flex items-center gap-2 mt-1">
             <span className="text-xs text-gray-500">
               {new Date(post.scheduled_date || "").toLocaleDateString()}
             </span>
             <span className="text-gray-300">•</span>
             <div className="flex items-center gap-1 text-blue-600 bg-blue-50 px-2 py-0.5 rounded-full text-[10px] font-bold">
                <Facebook size={10} fill="currentColor" />
                <span>Facebook</span>
             </div>
          </div>

        </div>
        
        <div className={`px-2.5 py-1 rounded-md text-xs font-bold border ${
            Number(averageScore) > 0.7 ? "bg-green-50 text-green-700 border-green-100" : "bg-yellow-50 text-yellow-700 border-yellow-100"
        }`}>
          Score: {averageScore}
        </div>
      </div>

      {loading ? (
        <div className="flex-1 flex items-center justify-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      ) : stats ? (
        <>
            <div className="flex-1 min-h-0 relative">
            <Bar
                data={{
                labels: ["Likes", "Comm", "Share", "Reach"],
                datasets: [
                    {
                    label: "Metrics",
                    data: [stats.likes, stats.comments, stats.shares, stats.reach === -1 ? 0 : stats.reach],
                    backgroundColor: ["#3B82F6", "#10B981", "#F59E0B", "#8B5CF6"],
                    borderRadius: 6,
                    barThickness: 25,
                    },
                ],
                }}
                options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { display: false }, ticks: { font: { size: 10 } } },
                    y: { display: false, grid: { display: false } }
                }
                }}
            />
            </div>

            <div className="mt-4 pt-4 border-t border-gray-100 grid grid-cols-4 gap-2">
                <div className="flex flex-col items-center p-2 rounded-lg bg-blue-50/50">
                    <div className="flex items-center gap-1 text-blue-600 mb-1"><ThumbsUp size={12} /><span className="text-[10px] font-bold">Like</span></div>
                    <span className="text-sm font-bold text-gray-700">{stats.likes}</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-green-50/50">
                    <div className="flex items-center gap-1 text-green-600 mb-1"><MessageCircle size={12} /><span className="text-[10px] font-bold">Comm</span></div>
                    <span className="text-sm font-bold text-gray-700">{stats.comments}</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-orange-50/50">
                    <div className="flex items-center gap-1 text-orange-600 mb-1"><Share2 size={12} /><span className="text-[10px] font-bold">Share</span></div>
                    <span className="text-sm font-bold text-gray-700">{stats.shares}</span>
                </div>
                <div className="flex flex-col items-center p-2 rounded-lg bg-purple-50/50">
                    <div className="flex items-center gap-1 text-purple-600 mb-1"><Eye size={12} /><span className="text-[10px] font-bold">Reach</span></div>
                    <span className="text-sm font-bold text-gray-700">{stats.reach === -1 ? "-" : stats.reach}</span>
                </div>
            </div>
        </>
      ) : (
        <div className="flex-1 flex items-center justify-center text-gray-400 text-sm">
            Metrics unavailable
        </div>
      )}
    </div>
  );
};

export default SocialMetricsChart;