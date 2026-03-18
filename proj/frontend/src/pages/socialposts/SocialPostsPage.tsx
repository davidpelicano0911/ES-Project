import { useState, useEffect, useMemo } from "react";
import EmptyState from "../../components/states/EmptyState";
import { Mail, Plus, ChevronLeft, ChevronRight, Grid3X3, Calendar } from "lucide-react";
import { useNavigate } from "react-router-dom";
import DeleteConfirmModal from "../../components/modals/DeleteConfirmModal";
import LoadingState from "../../components/states/LoadingState";
import SuccessMessage from "../../components/messages/SuccessMessage";
import FailMessage from "../../components/messages/FailMessage";
import SearchBar from "../../components/searchandfilters/SearchBar";
import { useUser } from "../../context/UserContext";
import type { Post } from "../../types/post";
import { deletePost, getPosts } from "../../api/apiPosts";
import facebookImage from "../../assets/facebook.png";
import instagramImage from "../../assets/instagram.png";
import twitterImage from "../../assets/twitter.svg";
import SocialPostCard from "../../components/cards/SocialPostCard";
import SocialPostModal from "../../components/modals/SocialPostModal";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";


const SocialPostsPage = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);
  const [calendarView, setCalendarView] = useState<"month" | "week">("month");
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewType, setViewType] = useState<"grid" | "calendar">("grid");
  const { hasRole } = useUser();
  const tracer = useTracer();

  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [postToDelete, setPostToDelete] = useState<Post | null>(null);
  const [postToView, setPostToView] = useState<Post | null>(null);


  const navigate = useNavigate();

  useEffect(() => {
    tracer.startActiveSpan("Page.SocialPosts.Load", (span) => {
      span.setAttribute("page.name", "SocialPostsPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  useEffect(() => {
    const fetchPosts = async () => {
      tracer.startActiveSpan("API.GetPosts", async (span) => {
        try {
          setLoading(true);
          const data = await getPosts();
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
          span.addEvent("posts_fetched");
          setPosts(data || []);
        } catch (err) {
          console.error("Failed to load posts", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR, message: (err as Error).message });
          span.addEvent("posts_fetch_failed");
          setError("Failed to fetch posts");
        } finally {
          setLoading(false);
          span.end();
        }
      });
    };

    fetchPosts();
  }, []);

  const filtered = posts.filter((post) =>
    tracer.startActiveSpan("User.ACTION.FilterPosts", (span) => {
      const matches = post.name.toLowerCase().includes(search.toLowerCase());
      span.setAttribute("search.query", search);
      span.setAttribute("search.result", matches);
      span.end();
      return matches;
    })
  );

  const getDisplayText = () => {
    const month = currentDate.toLocaleString("default", { month: "long", year: "numeric" });
    if (calendarView === "month") {
      return month;
    } else {
      const weekStart = new Date(currentDate);
      const day = weekStart.getDay();
      const diff = weekStart.getDate() - day + (day === 0 ? -6 : 1);
      weekStart.setDate(diff);
      const weekEnd = new Date(weekStart);
      weekEnd.setDate(weekEnd.getDate() + 6);
      return `${weekStart.toLocaleString("default", { month: "short", day: "numeric" })} - ${weekEnd.toLocaleString("default", { month: "short", day: "numeric" })}`;
    }
  };

  const handlePrevious = () => {
    tracer.startActiveSpan("User.ACTION.NavigatePrevious", (span) => {
      const newDate = new Date(currentDate);
      if (calendarView === "month") {
        newDate.setMonth(newDate.getMonth() - 1);
      } else {
        newDate.setDate(newDate.getDate() - 7);
      }
      setCurrentDate(newDate);

      span.setAttribute("calendar.view", calendarView);
      span.addEvent("navigating_previous");
      span.setAttribute("new.current.date", newDate.toISOString());
      span.end();
    });
  };

  const handleNext = () => {
    tracer.startActiveSpan("User.ACTION.NavigateNext", (span) => {
      const newDate = new Date(currentDate);
      if (calendarView === "month") {
        newDate.setMonth(newDate.getMonth() + 1);
      } else {
        newDate.setDate(newDate.getDate() + 7);
      }
      setCurrentDate(newDate);

      span.setAttribute("calendar.view", calendarView);
      span.addEvent("navigating_next");
      span.setAttribute("new.current.date", newDate.toISOString());
      span.end();
    });
  };

  const scheduledPosts = useMemo(() => {
    let startDate: Date;
    let endDate: Date;

    if (calendarView === "month") {
      startDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      endDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0, 23, 59, 59);
    } else {
      startDate = new Date(currentDate);
      const day = startDate.getDay();
      const diff = startDate.getDate() - day + (day === 0 ? -6 : 1);
      startDate.setDate(diff);
      startDate.setHours(0, 0, 0, 0);
      
      endDate = new Date(startDate);
      endDate.setDate(endDate.getDate() + 6);
      endDate.setHours(23, 59, 59, 999);
    }

    return posts
      .filter((post) => {
        const postDate = new Date(post.scheduled_date);
        return postDate >= startDate && postDate <= endDate;
      })
      .sort((a, b) => new Date(a.scheduled_date).getTime() - new Date(b.scheduled_date).getTime());
  }, [posts, calendarView, currentDate]);

  const getScheduledPosts = () => {
    tracer.startActiveSpan("User.ACTION.GetScheduledPosts", (span) => {
      span.setAttribute("calendar.view", calendarView);
      span.setAttribute("current.date", currentDate.toISOString());
      span.addEvent("fetching_scheduled_posts");
      span.end();
    });
    return scheduledPosts;
  };

  const confirmDelete = async () => {
    tracer.startActiveSpan("API.DeletePost", async (span) => {
      if (!postToDelete) {
        span.addEvent("no_post_to_delete");
        span.end();
        return;
      }
      try {
        await deletePost(postToDelete.id);
        setPosts((prev) => prev.filter((p) => p.id !== postToDelete.id));
        setIsDeleteModalOpen(false);
        span.setAttribute("post.id", postToDelete.id);
        span.addEvent("delete_modal_closed");
        span.setAttribute("api.success", true);
        span.setStatus({ code: SpanStatusCode.OK });
        span.addEvent("post_deleted_successfully");
        setToastMessage({
          type: "success",
          text: `Post "${postToDelete.name}" deleted`, 
        });
      } catch (err) {
        console.error("Failed to delete post", err);
        span.setAttribute("api.success", false);
        span.recordException(err as Error);
        span.setStatus({ code: SpanStatusCode.ERROR, message: String(err) });
        span.addEvent("post_delete_failed");
        setToastMessage({
          type: "fail",
          text: `Failed to delete "${postToDelete.name}!"`, 
        });
      } finally {
        setTimeout(() => setToastMessage(null), 3000);
        span.end();
      }
    });
  };

  

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col relative">
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

      <div className="bg-white shadow-[0_2px_6px_rgba(0,0,0,0.05)] px-8 py-6 mx-8 mt-4 rounded-2xl flex justify-between items-center">
        <div>
          <h1 className="text-[22px] font-semibold text-[#111827]">
            Social Media Posts
          </h1>
          <p className="text-[#6B7280] text-sm">
            Create and manage your social media posts.
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* View Type Toggle */}
          <div className="flex bg-[#F3F4F6] rounded-lg p-1 gap-1">
            <button
              onClick={() => setViewType("grid")}
              className={`p-2 rounded transition ${
          viewType === "grid"
            ? "bg-white text-[#2563EB] shadow-sm"
            : "text-[#6B7280] hover:text-[#111827]"
              }`}
              title="Grid view"
            >
              <Grid3X3 size={18} />
            </button>

            <button
              onClick={() => setViewType("calendar")}
              className={`p-2 rounded transition ${
                viewType === "calendar"
                  ? "bg-white text-[#2563EB] shadow-sm"
                  : "text-[#6B7280] hover:text-[#111827]"
              }`}
              title="Calendar view"
                  >
              <Calendar size={18} />
            </button>
          </div>

          {hasRole("CONTENT_MARKETER") && (
            <button
              onClick={() => navigate("/app/social-posts/create")}
              className="flex items-center gap-2 bg-[#2563EB] hover:bg-[#1D4ED8] transition text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm"
            >
              <Plus className="h-4 w-4" />
              Create Post
            </button>
          )}
        </div>
      </div>

      <div className="flex-1 p-8">
        {loading ? (
          <LoadingState message="Loading posts..." />
        ) : error ? (
          <FailMessage entity={error} onClose={() => setError(null)} />
        ) : (
          <>
            {/* Search bar sempre visível no modo grid */}
            {viewType === "grid" && (
              <div className="mb-6 flex justify-start">
                <SearchBar
                  value={search}
                  onChange={setSearch}
                  placeholder="Search posts..."
                />
              </div>
            )}

            {/* Caso não existam resultados */}
            {filtered.length === 0 && viewType !== "calendar" ? (
              <div className="bg-white border border-[#E5E7EB] rounded-xl shadow-sm py-16">
                <EmptyState
                  icon={<Mail size={48} />}
                  title="No Social Media Posts Found"
                  description="No posts were found. Try adjusting your search or create a new one."
                />
              </div>
            ) : viewType === "grid" ? (
              /* GRID VIEW */
              <div className="grid gap-6 mt-4 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3">
                {filtered.map((post) => (
                  <SocialPostCard
                    key={post.id}
                    post={post}
                    onDelete={(p) => {
                      setPostToDelete(p);
                      setIsDeleteModalOpen(true);
                    }}
                    onView={(p) => setPostToView(p)}
                  />
                ))}
              </div>
            ) : (
              /* CALENDAR VIEW */
              <div className="bg-white border border-[#E5E7EB] rounded-lg p-6 shadow-sm mt-4">
                {/* Top Bar */}
                <div className="flex justify-between items-center mb-6 pb-4 border-b border-[#E5E7EB]">
                  <div>
                    <h2 className="text-lg font-semibold text-[#111827]">Content Calendar</h2>
                    <p className="text-sm text-[#6B7280]">Manage your scheduled posts</p>
                  </div>

                  {/* Month/Week Switch */}
                  <div className="flex bg-[#F3F4F6] rounded-lg p-1">
                    <button
                      onClick={() => setCalendarView("month")}
                      className={`px-4 py-2 rounded font-medium text-sm transition ${
                        calendarView === "month"
                          ? "bg-white text-[#2563EB] shadow-sm"
                          : "text-[#6B7280] hover:text-[#111827]"
                      }`}
                    >
                      Month
                    </button>
                    <button
                      onClick={() => setCalendarView("week")}
                      className={`px-4 py-2 rounded font-medium text-sm transition ${
                        calendarView === "week"
                          ? "bg-white text-[#2563EB] shadow-sm"
                          : "text-[#6B7280] hover:text-[#111827]"
                      }`}
                    >
                      Week
                    </button>
                  </div>
                </div>

                {/* Navigation Bar */}
                <div className="flex items-center justify-center gap-4 mb-6">
                  <button
                    onClick={handlePrevious}
                    className="p-2 hover:bg-[#F3F4F6] rounded-lg transition"
                  >
                    <ChevronLeft size={20} className="text-[#6B7280]" />
                  </button>

                  <span className="text-lg font-semibold text-[#111827] min-w-[200px] text-center">
                    {getDisplayText()}
                  </span>

                  <button
                    onClick={handleNext}
                    className="p-2 hover:bg-[#F3F4F6] rounded-lg transition"
                  >
                    <ChevronRight size={20} className="text-[#6B7280]" />
                  </button>
                </div>

                {/* Scheduled Posts List */}
                <div className="space-y-3">
                  {getScheduledPosts().length === 0 ? (
                    <div className="text-center py-8">
                      <p className="text-[#6B7280] text-sm">
                        No posts scheduled for this period
                      </p>
                    </div>
                  ) : (
                    getScheduledPosts().map((post) => (
                      <div
                        key={post.id}
                        className="border border-[#E5E7EB] rounded-lg p-4 bg-white hover:shadow-md transition grid grid-cols-3 gap-4 items-stretch"
                      >
                        {/* Column 1: Title and Description */}
                        <div className="flex flex-col justify-center">
                          <h3 className="font-semibold text-[#111827] text-base mb-2">
                            {post.name}
                          </h3>
                          <p className="text-sm text-[#6B7280] line-clamp-3">
                            {post.description}
                          </p>
                        </div>

                        {/* Column 2: Date */}
                        <div className="flex items-center justify-center">
                          <p className="text-2xl font-bold text-[#111827] text-center">
                            {new Date(post.scheduled_date).toLocaleDateString("en-US", {
                              month: "short",
                              day: "numeric",
                              year: "numeric",
                            })}
                          </p>
                        </div>

                        {/* Column 3: Platforms */}
                        <div className="flex flex-wrap gap-2 items-center justify-end">
                          {post.platforms.map((platform, index) => {
                            let platformIcon;
                            switch (platform.platformType.toLowerCase()) {
                              case "facebook":
                                platformIcon = facebookImage;
                                break;
                              case "instagram":
                                platformIcon = instagramImage;
                                break;
                              case "twitter":
                                platformIcon = twitterImage;
                                break;
                              default:
                                platformIcon = null;
                            }

                            return (
                              <div
                                key={index}
                                className="border border-[#D1D5DB] rounded-md px-3 py-2 bg-[#F9FAFB] flex flex-col items-center gap-1"
                              >
                                {platformIcon && (
                                  <img
                                    src={platformIcon}
                                    alt={platform.platformType}
                                    className="w-5 h-5"
                                  />
                                )}
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
                            );
                          })}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </div>


      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        title="Delete Post"
        entityName={postToDelete?.id.toString() || ""}
        message={`Are you sure you want to permanently delete "${
          postToDelete?.id || "this post"
        }"? This action cannot be undone.`}
        confirmText="Delete Post"
        cancelText="Cancel"
        onConfirm={() => {
          void confirmDelete();
        }}
        onCancel={() => setIsDeleteModalOpen(false)}
      />

      {postToView && (
        <SocialPostModal
          isOpen={!!postToView}
          post={postToView}
          onClose={() => setPostToView(null)}
        />
      )}

    </div>
  );
};

export default SocialPostsPage;