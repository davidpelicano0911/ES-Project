import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../api/apiConfig";
import LoadingState from "../../components/states/LoadingState";
import FailMessage from "../../components/messages/FailMessage";
import type { LandingPage } from "../../types/landingPage";
import {
  extractTrackingMetadata,
  storeTrackingMetadata,
  formatMetadataForLogging,
} from "../../utils/trackingMetadata";
import { getStoredTrackingMetadata } from "../../utils/trackingMetadata";

const PublicLandingPageViewPage = () => {
  const { id } = useParams<{ id: string }>();
  const [page, setPage] = useState<LandingPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initializePage = async () => {
      try {
        // Extract and store tracking metadata from URL
        const extractedMetadata = extractTrackingMetadata();
        storeTrackingMetadata(extractedMetadata);

        // Log tracking metadata for debugging
        console.log(
          "Public Landing Page View - Tracking metadata:",
          formatMetadataForLogging(extractedMetadata)
        );

        if (!id) {
          setError("Landing page ID is required");
          setLoading(false);
          return;
        }

        // Fetch landing page from public endpoint (no authentication required)
        const response = await api.get(`/landing-pages/public/${id}`);
        const foundPage = response.data;

        if (!foundPage) {
          setError("Landing page not found");
        } else {
          setPage(foundPage);
          // Send a tracking event that the landing page was opened
          try {
            const stored = getStoredTrackingMetadata();
            const userIdentifier = stored.user_id || stored.lead_id || stored.email || null;
            const metadata = {
              landingPageId: String(id),
              user: userIdentifier,
            };
            await api.post(`/events`, {
              eventType: "LANDING_PAGE_OPENED",
              metadata,
            });
            console.log("Sent LANDING_PAGE_OPENED event", metadata);
          } catch (err) {
            console.warn("Failed to send LANDING_PAGE_OPENED event", err);
          }
        }
      } catch (err) {
        console.error("Error loading landing page:", err);
        setError("Failed to load landing page. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    initializePage();
  }, [id]);

  // Track readability (80% of page scrolled) and send a one-time event
  useEffect(() => {
    if (!page) return;
    const sentKey = `landingpage_readability_sent_${id}`;
    let sent = Boolean(localStorage.getItem(sentKey));

    const onScroll = async () => {
      try {
        const scrollTop = window.scrollY || window.pageYOffset;
        const docHeight = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight);
        const winHeight = window.innerHeight || document.documentElement.clientHeight;
        const scrolled = (scrollTop + winHeight) / docHeight;
        if (!sent && scrolled >= 0.8) {
          sent = true;
          try { localStorage.setItem(sentKey, '1'); } catch(e) { /* ignore */ }
          const stored = getStoredTrackingMetadata();
          const userIdentifier = stored.user_id || stored.lead_id || stored.email || null;
          const metadata = {
            landingPageId: String(id),
            user: userIdentifier,
            readability: "80%",
          };
          try {
            await api.post(`/events`, {
              eventType: "LANDING_PAGE_READABILITY_80",
              metadata,
            });
            console.debug("Sent LANDING_PAGE_READABILITY_80 event", metadata);
          } catch (err) {
            console.warn("Failed to send LANDING_PAGE_READABILITY_80 event", err);
          }
        }
      } catch (e) {
        // ignore
      }
    };

    window.addEventListener("scroll", onScroll, { passive: true });

    // also check immediately in case short pages
    onScroll();

    return () => {
      window.removeEventListener("scroll", onScroll);
    };
  }, [page, id]);

  if (loading) {
    return <LoadingState message="Loading landing page..." />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <FailMessage
          entity={error}
          onClose={() => setError(null)}
        />
      </div>
    );
  }

  if (!page) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            Landing Page Not Found
          </h1>
          <p className="text-gray-600">
            The landing page you're looking for doesn't exist or has been removed.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full min-h-screen bg-white">
      {/* Landing Page Body - Full Width */}
      {page.body ? (
        <div
          className="w-full"
          dangerouslySetInnerHTML={{ __html: page.body }}
        />
      ) : page.design ? (
        <div className="w-full">
          {/* 
            Render the design/layout from database if available
          */}
          <div className="w-full h-screen flex items-center justify-center">
            <p className="text-center text-gray-500">Landing page design content</p>
          </div>
        </div>
      ) : (
        <div className="w-full h-screen flex items-center justify-center">
          <p className="text-center text-gray-500">This landing page has no content</p>
        </div>
      )}

    </div>
  );
};

export default PublicLandingPageViewPage;
