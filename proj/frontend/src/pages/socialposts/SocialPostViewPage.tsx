import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getPostById, getImage } from "../../api/apiPosts";
import type { Post } from "../../types/post";
import { ArrowLeft, Calendar, Image as ImageIcon } from "lucide-react";

export default function SocialPostViewPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState<Post | null>(null);
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;

    const load = async () => {
      try {
        const data = await getPostById(Number(id));
        setPost(data);

        // Load the image if available
        if (data.file_path) {
          try {
            const url = await getImage(data.file_path);
            setImageUrl(url);
          } catch (err) {
            console.warn("Failed to load post image:", err);
          }
        }
      } catch (err) {
        console.error("Failed to load post:", err);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  if (loading) {
    return <p className="p-6 text-gray-500">Loading post...</p>;
  }

  if (!post) {
    return <p className="p-6 text-red-500">Post not found.</p>;
  }

  return (
    <div className="p-10 bg-white max-w-4xl mx-auto rounded-xl shadow-sm">
      {/* BACK BUTTON */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </button>

      {/* TITLE */}
      <h1 className="text-2xl font-semibold text-gray-900 mb-2">
        {post.name}
      </h1>

      {/* DESCRIPTION */}
      <p className="text-sm text-gray-500 mb-6">
        {post.description || "No description available."}
      </p>

      {/* SCHEDULED DATE */}
      <div className="flex items-center gap-2 text-sm text-gray-700 mb-4">
        <Calendar className="w-4 h-4" />
        Scheduled for:{" "}
        {post.scheduled_date
          ? new Date(post.scheduled_date).toLocaleString("en-GB")
          : "Not scheduled"}
      </div>

      {/* IMAGE */}
      <h2 className="text-lg font-medium mt-6 mb-2">Post Image</h2>
      <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm flex justify-center">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt="Post visual"
            className="max-w-full rounded-lg shadow"
          />
        ) : (
          <div className="text-gray-400 flex flex-col items-center gap-2">
            <ImageIcon className="w-10 h-10" />
            <p>No image available.</p>
          </div>
        )}
      </div>

  
    </div>
  );
}
