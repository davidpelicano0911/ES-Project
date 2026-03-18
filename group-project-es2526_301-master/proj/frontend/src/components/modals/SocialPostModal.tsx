import { X } from "lucide-react";
import SuccessMessage from "../messages/SuccessMessage";
import FailMessage from "../messages/FailMessage";
import PostImage from "../../pages/socialposts/PostImage";
import facebookImage from "../../assets/facebook.png";
import instagramImage from "../../assets/instagram.png";
import twitterImage from "../../assets/twitter.svg";
import type { Post } from "../../types/post";
import { useEffect, useState } from "react";
import { getPostStats } from "../../api/apiPosts";
import { Bar } from "react-chartjs-2";
import { ExternalLink } from "lucide-react";
import { useFlags } from "flagsmith/react";
import {useUser} from "../../context/UserContext";


import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

interface SocialPostModalProps {
  isOpen: boolean;
  post: Post;
  onClose: () => void;
}

const SocialPostModal = ({ isOpen, post, onClose }: SocialPostModalProps) => {
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);

  if (!isOpen || !post) return null;

  const PLATFORM_ICONS: Record<string, string> = {
    facebook: facebookImage,
    instagram: instagramImage,
    twitter: twitterImage,
  };

  const formatDate = (isoString?: string) => {
    if (!isoString) return "Unknown date";
    const date = new Date(isoString);
    return date.toLocaleString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const [stats, setStats] = useState<any>(null);
  const [loadingStats, setLoadingStats] = useState(false);
  const [statsError, setStatsError] = useState<string | null>(null);

  useEffect(() => {
    if (!isOpen || !post) return;

    setStats(null);
    setStatsError(null);
    setLoadingStats(false);

    const facebookPlatform = post.platforms.find(
      (p) => p.platformType.toLowerCase() === "facebook"
    );

    if (!facebookPlatform || facebookPlatform.status !== "PUBLISHED") {
      setStatsError("Post not published yet");
      return;
    }

    setLoadingStats(true);

    getPostStats(post.id)
      .then((data) => setStats(data))
      .catch(() => setStatsError("Metrics unavailable"))
      .finally(() => setLoadingStats(false));

  }, [isOpen, post]);



  const facebookPlatform = post.platforms.find(
    (p) => p.platformType.toLowerCase() === "facebook"
  );

  const postUrl = facebookPlatform?.postUrl || null;

  const flags = useFlags(["enable_post_metrics"]);
  const enablePostMetrics = flags.enable_post_metrics.enabled;
  const { hasRole } = useUser();


  return (
    <>
      {toastMessage && (
        <>
          {toastMessage.type === "success" ? (
            <SuccessMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          ) : (
            <FailMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          )}
        </>
      )}

      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div className="bg-white w-[900px] max-h-[90vh] rounded-2xl shadow-xl p-8 overflow-y-auto animate-fadeIn relative">
          {/* Header */}
          <div className="flex justify-between items-center mb-5">
            <div>
              <h2 className="text-xl font-semibold text-[#111827]">
                {post.name || "Untitled Post"}
              </h2>
              <p className="text-sm text-[#6B7280]">
                <b>Scheduled for:</b> {formatDate(post.scheduled_date)}
              </p>
            </div>
            <button
              onClick={onClose}
              className="text-[#6B7280] hover:text-[#111827] p-2 rounded-md hover:bg-gray-100 transition-all cursor-pointer"
              title="Close"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          {/* Image and description */}
          <div className="border border-[#E5E7EB] rounded-lg overflow-hidden bg-white shadow-inner mb-6">
            {post.file_path ? (
              <PostImage filename={post.file_path} alt={post.name} />
            ) : (
              <div className="w-full h-60 bg-gray-100 flex items-center justify-center text-gray-400">
                No image available
              </div>
            )}
          </div>

          <p className="text-gray-700 mb-8 whitespace-pre-wrap leading-relaxed">
            {post.description || "No description provided for this post."}
          </p>

          {/* Platforms */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-8">
            {post.platforms.map((platform, index) => (
              <div key={index} className="border border-[#D1D5DB] rounded-md p-3 bg-[#F9FAFB] shadow-sm">
                
                {/* Linha principal */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    {PLATFORM_ICONS[platform.platformType.toLowerCase()] && (
                      <img
                        src={PLATFORM_ICONS[platform.platformType.toLowerCase()]}
                        alt={platform.platformType}
                        className="w-5 h-5"
                      />
                    )}
                    <span className="capitalize text-sm text-[#111827] font-medium">
                      {platform.platformType}
                    </span>
                  </div>

                  <span
                    className={`text-xs font-semibold ${
                      platform.status === "PUBLISHED"
                        ? "text-green-600"
                        : platform.status === "FAILED"
                        ? "text-red-600"
                        : "text-yellow-600"
                    }`}
                  >
                    {platform.status}
                  </span>
                </div>

                {/* Performance Score */}
                {hasRole("MARKETING_ANALYST") && enablePostMetrics && platform.performanceScore != null && (
                  <div className="flex items-center justify-between mt-2">
                    <span className="text-xs text-gray-600">Performance Score</span>

                    <span
                      className={`
                        text-xs font-bold px-2 py-1 rounded-md
                        ${(platform.performanceScore ?? 0) > 0.7 ? "bg-green-100 text-green-700" :
                          (platform.performanceScore ?? 0) > 0.4 ? "bg-yellow-100 text-yellow-700" :
                          "bg-red-100 text-red-700"}
                      `}
                    >
                      {(platform.performanceScore ?? 0).toFixed(2)}
                    </span>
                  </div>
                )}

              </div>
            ))}

          </div>



          {hasRole("MARKETING_ANALYST") && enablePostMetrics && postUrl && (
            <div className="mb-8 flex items-center">
              <a
                href={postUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-2 bg-blue-600 hover:bg-blue-700 
                          text-white px-4 py-2 rounded-lg text-sm font-semibold shadow-sm cursor-pointer"
              >
                <ExternalLink className="w-4 h-4 text-white" />
                Visit Facebook Post
              </a>
            </div>
          )}



          {/* Engagement Metrics */}
          {hasRole("MARKETING_ANALYST") && enablePostMetrics && (
            <div className="mb-8">
              <h3 className="text-lg font-semibold text-[#111827] mb-4">Engagement Metrics</h3>

              {loadingStats && <p className="text-gray-500">Loading metrics...</p>}

              {statsError && (
                <p className="text-red-600 font-medium">{statsError}</p>
              )}

              {stats && (
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                  <div className="bg-[#F3F4F6] p-4 rounded-lg text-center shadow-sm">
                    <p className="text-xl font-bold text-[#111827]">{stats.likes}</p>
                    <p className="text-xs text-gray-600">Likes</p>
                  </div>

                  <div className="bg-[#F3F4F6] p-4 rounded-lg text-center shadow-sm">
                    <p className="text-xl font-bold text-[#111827]">{stats.comments}</p>
                    <p className="text-xs text-gray-600">Comments</p>
                  </div>

                  <div className="bg-[#F3F4F6] p-4 rounded-lg text-center shadow-sm">
                    <p className="text-xl font-bold text-[#111827]">{stats.shares}</p>
                    <p className="text-xs text-gray-600">Shares</p>
                  </div>

                  <div className="bg-[#F3F4F6] p-4 rounded-lg text-center shadow-sm">
                    <p className="text-xl font-bold text-[#111827]">
                      {stats.reach === -1 ? "⏳" : stats.reach}
                    </p>
                    <p className="text-xs text-gray-600">Reach</p>
                  </div>
                </div>
              )}

              {stats?.reach === -1 && (
                <p className="text-yellow-600 mt-2 text-sm">
                  Facebook reach data may take up to 15 minutes to update.
                </p>
              )}
            </div>
          )}
  

          {hasRole("MARKETING_ANALYST") && enablePostMetrics && stats && (
            <div className="mt-8">
              <Bar
                data={{
                  labels: ["Likes", "Comments", "Shares", "Reach"],
                  datasets: [
                    {
                      label: "Engagement Metrics",
                      data: [
                        stats.likes,
                        stats.comments,
                        stats.shares,
                        stats.reach === -1 ? 0 : stats.reach
                      ],
                      backgroundColor: ["#2563EB", "#10B981", "#F59E0B", "#8B5CF6"],
                    },
                  ],
                }}
                options={{
                  responsive: true,
                  plugins: { legend: { display: false } },
                }}
              />
            </div>
          )}
          

        </div>
      </div>
    </>
  );
};

export default SocialPostModal;