import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getLandingPageById } from "../../api/apiLandingPages";
import type { LandingPage } from "../../types/landingPage";
import { ArrowLeft } from "lucide-react";

export default function LandingPageViewPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [page, setPage] = useState<LandingPage | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;

    const load = async () => {
      try {
        const data = await getLandingPageById(Number(id));
        setPage(data);
      } catch (err) {
        console.error("Failed to load landing page:", err);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  if (loading) {
    return <p className="p-6 text-gray-500">Loading landing page...</p>;
  }

  if (!page) {
    return <p className="p-6 text-red-500">Landing page not found.</p>;
  }

  return (
    <div className="p-10 bg-white max-w-5xl mx-auto rounded-xl shadow-sm">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </button>

      <h1 className="text-2xl font-semibold text-gray-900 mb-2">
        {page.name}
      </h1>

      <p className="text-sm text-gray-500 mb-6">
        {page.description || "No description available."}
      </p>

      <h2 className="text-lg font-medium mt-4 mb-2">Landing Page Preview</h2>

      <div
        className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm"
        dangerouslySetInnerHTML={{ __html: page.body || "<p>No content.</p>" }}
      ></div>
    </div>
  );
}
