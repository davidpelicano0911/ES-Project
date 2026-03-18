import { Eye, Trash2 } from "lucide-react";
import { useUser } from "../../context/UserContext";
import type { Post } from "../../types/post";
import { useFlags } from "flagsmith/react";
import RemoveFromCampaignButton from "../buttons/RemoveFromCampaignButton";

interface SocialPostCardProps {
  post: Post;
  onDelete?: (post: Post) => void;
  onView: (post: Post) => void;
  onRemove?: (post: Post) => void;
  isInCampaign?: boolean;
}

const SocialPostCard = ({ post, onDelete, onView, onRemove, isInCampaign = false }: SocialPostCardProps) => {
  const { hasRole } = useUser();
  const flags = useFlags(["enable_delete_social_post", "enable_view_social_post"]);

  const canDelete = hasRole("CONTENT_MARKETER") && flags.enable_delete_social_post.enabled;
  const canView = flags.enable_view_social_post.enabled;

  const handleDelete = (e: React.MouseEvent) => {
    e.stopPropagation();
    onDelete?.(post);
  };

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    onRemove?.(post);
  };

  const formatDate = (isoString?: string) => {
    if (!isoString) return "Unknown date";
    const date = new Date(isoString);
    return date.toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-lg flex flex-col justify-between min-h-[260px] transition-all cursor-pointer">
      
      {/* TOP SECTION */}
      <div className="p-6 flex-1">
        <div className="flex justify-between items-start mb-3">
          <h3 className="text-lg font-semibold text-gray-900 leading-snug line-clamp-1">
            {post.name || "Untitled Post"}
          </h3>

          {!isInCampaign && canDelete && (
            <button
              onClick={handleDelete}
              className="p-1.5 hover:bg-red-50 rounded-md transition"
              title="Delete Post"
            >
              <Trash2 className="h-5 w-5 text-gray-500 hover:text-red-600 transition" />
            </button>
          )}
        </div>

        {/* DESCRIPTION */}
        <p className="text-sm text-gray-600 mb-3 line-clamp-3">
          {post.description || "No description available."}
        </p>

        {/* DATE (matches landing page design) */}
        <p className="text-xs text-gray-400">
          Scheduled for{" "}
          <span className="font-medium text-gray-500">
            {formatDate(post.scheduled_date)}
          </span>
        </p>
      </div>

      {/* FOOTER BUTTONS */}
      <div className="flex border-t border-gray-200 divide-x divide-gray-200">
        {isInCampaign && onRemove && hasRole("MARKETING_MANAGER") && (
          <RemoveFromCampaignButton
            onClick={handleRemove}
            label="Remove"
            rounded={true}
          />
        )}

        {canView && (
          <button
            onClick={() => onView(post)}
            className={`flex-1 flex items-center justify-center gap-2 py-3 text-sm font-medium text-white 
              bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 
              active:scale-[0.98] transition-all duration-150 
              ${!onRemove ? "rounded-b-2xl" : "rounded-br-2xl"}`}
          >
            <Eye className="h-4 w-4 text-white" />
            View
          </button>
        )}
      </div>
    </div>
  );
};

export default SocialPostCard;
