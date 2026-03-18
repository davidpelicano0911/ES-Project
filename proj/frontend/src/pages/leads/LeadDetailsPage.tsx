import { useEffect, useState, useMemo } from "react";
import { useParams } from "react-router-dom";
import type { Lead } from "../../types/lead";
import type { Activity } from "../../types/activity";
import { getLeadById } from "../../api/apiLeads";
import { getFormSubmissionsByLeadId, type FormSubmission } from "../../api/apiFormSubmissions";
import { getLandingPageEventsByLeadId } from "../../api/apiLandingPageEvents";
import type { LandingPageEvent } from "../../types/landingPageEvent";
import { getEmailLogsByLead, type EmailLog } from "../../api/apiEmailLogs";
import { Phone, Mail, FileText } from "lucide-react";
import ScoreCard from "../../components/cards/ScoreCard";
import LoadingState from "../../components/states/LoadingState";
import EmptyState from "../../components/states/EmptyState";
import FailMessage from "../../components/messages/FailMessage";
// import ScoreCard from "../../components/cards/ScoreCard";
import ActivityCard from "../../components/cards/ActivityCard";
import useTracer from "../../hooks/useTracer";
import { SpanStatusCode } from "@opentelemetry/api";

const timeAgo = (iso: string) => {
  const diff = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins} minutes ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours} hours ago`;
  const days = Math.floor(hours / 24);
  return `${days} days ago`;
};

const LeadDetailsPage = () => {
  const { id } = useParams();
  const leadId = Number(id);
  const [lead, setLead] = useState<Lead | null>(null);
  const [formSubmissions, setFormSubmissions] = useState<FormSubmission[]>([]);
  const [landingEvents, setLandingEvents] = useState<LandingPageEvent[]>([]);
  const [emailLogs, setEmailLogs] = useState<EmailLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState("timeline");
  // const [syncing, setSyncing] = useState(false);
  // const [lastSync, setLastSync] = useState<string>("5 minutes ago");
  const tracer = useTracer();

  useEffect(() => {
    tracer.startActiveSpan("Page.LeadDetails.Load", (span: any) => {
      span.setAttribute("page.name", "LeadDetailsPage");
      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.setStatus({ code: SpanStatusCode.OK });
        span.end();
      };
    });
  }, [tracer]);

  // const score = 78;

  // Convert form submissions to activities
  const activities: Activity[] = useMemo(() => {
    const formActivities: Activity[] = formSubmissions.map((submission) => ({
      id: `form-${submission.id}`,
      type: "form",
      title: `Submitted form: "${submission.form?.name || "Form"}"`,
      description: submission.responsesJson ? `Responses: ${submission.responsesJson}` : "Form submission",
      timestamp: submission.submittedAt,
    }));

    const landingActivities: Activity[] = landingEvents.map((evt) => ({
      id: `landing-${evt.id}`,
      type: "landing",
      title: evt.eventType ? evt.eventType.replace(/_/g, " ") : "Landing Page Event",
      description: evt.landingPageId ? `Landing page id: ${evt.landingPageId}` : undefined,
      timestamp: evt.createdAt,
    }));

    // Convert email logs to activities
    const emailActivities: Activity[] = emailLogs.map((log) => {
      const eventLabel = log.eventType.charAt(0) + log.eventType.slice(1).toLowerCase();
      let title = `Email ${eventLabel}`;
      let description = `To: ${log.emailAddress}`;
      
      if (log.subject) {
        title = `${eventLabel}: "${log.subject}"`;
      }
      
      return {
        id: `email-${log.id}`,
        type: "email",
        title,
        description,
        timestamp: log.createdAt,
      };
    });

    // Combine and sort by timestamp (newest first)
    return [...formActivities, ...landingActivities, ...emailActivities].sort((a, b) => {
      return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
    });
  }, [formSubmissions, landingEvents, emailLogs]);

  useEffect(() => {
    if (!isFinite(leadId) || leadId <= 0) return;
    const fetch = async () => {
      tracer.startActiveSpan("API.GetLeadDetails", async (span: any) => {
        try {
          const [leadData, submissionsData, landingData, emailData] = await Promise.all([
            getLeadById(leadId),
            getFormSubmissionsByLeadId(leadId),
            getLandingPageEventsByLeadId(leadId),
            getEmailLogsByLead(leadId),
          ]);
          setLead(leadData);
          setFormSubmissions(submissionsData);
          setLandingEvents(landingData);
          setEmailLogs(emailData);
          span.addEvent("lead_details_fetched");
          span.setAttribute("lead.id", leadId);
          span.setAttribute("api.success", true);
          span.setStatus({ code: SpanStatusCode.OK });
        } catch {
          setError("Failed to load lead details");
          span.setAttribute("lead.id", leadId);
          span.setAttribute("api.success", false);
          span.recordException(new Error("API call failed"));
          span.setStatus({ code: SpanStatusCode.ERROR, message: "API call failed" });
          span.addEvent("lead_details_fetch_failed");
        } finally {
          setLoading(false);
          span.end();
        }
      });
    };
    void fetch();
  }, [leadId]);

  // const handleSync = () => {
  //   setSyncing(true);
  //   setTimeout(() => {
  //     setSyncing(false);
  //     setLastSync("just now");
  //   }, 1000);
  // };

  // Filter activities based on selected tab
  const filteredActivities = useMemo(() => {
    switch (tab) {
      case "forms":
        return activities.filter((a) => a.type === "form");
      case "emails":
        return activities.filter((a) => a.type === "email");
      case "landing pages":
        return activities.filter((a) => a.type === "landing" || a.type === "website" || a.type === "link");
      default:
        return activities;
    }
  }, [activities, tab]);

    // enquanto flagsmith ainda não carregou → não renderizar




  if (loading) return <LoadingState message="Loading lead details..." />;
  if (error)
    return (
      <div className="p-8">
        <FailMessage entity={error} onClose={() => {}} />
      </div>
    );
  if (!lead)
    return (
      <div className="p-8">
        <EmptyState title="Lead not found" description="The requested lead does not exist." />
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      {/* Header */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6 flex flex-col sm:flex-row justify-between gap-4">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">
        {lead?.firstName || lead?.lastName
          ? `${lead?.firstName ?? ""} ${lead?.lastName ?? ""}`.trim()
          : "Not available"}
          </h2>
          
          <div className="flex items-center flex-wrap gap-6 mt-4 text-sm text-gray-700">
        <div className="flex items-center gap-2">
          <Mail className="h-4 w-4 text-gray-500" />
          <span>{lead?.email ?? "Not available"}</span>
        </div>
        <div className="flex items-center gap-2">
          <Phone className="h-4 w-4 text-gray-500" />
          <span>{lead?.phoneNumber ?? "Not available"}</span>
        </div>
        <div className="flex items-center gap-2">
          <FileText className="h-4 w-4 text-gray-500" />
          <span>{lead?.country ?? "Not available"}</span>
        </div>
          </div>
        </div>

        <div className="flex flex-col sm:items-end gap-3">
          {/* <select className="border rounded-md px-3 py-1 text-sm">
            <option>Contacted</option>
            <option>New</option>
            <option>Qualified</option>
          </select> */}
          {/* <div className="flex gap-2">
            <button className="flex items-center gap-2 bg-blue-600 text-white px-3 py-2 rounded-md text-sm">
              <Phone className="h-4 w-4" /> Call
            </button>
            <button className="flex items-center gap-2 bg-gray-100 text-gray-800 px-3 py-2 rounded-md text-sm">
              <Mail className="h-4 w-4" /> Email
            </button>
            <button className="flex items-center gap-2 bg-gray-100 text-gray-800 px-3 py-2 rounded-md text-sm">
              <Calendar className="h-4 w-4" /> Schedule
            </button>
          </div> */}
        </div>
      </div>

      {/* Lead Score */}
      <div className="mb-6">
        {lead && (
          <ScoreCard
            score={lead.score ?? (formSubmissions.length * 50 + landingEvents.length * 10 + (lead.isSubscribed ? 20 : 0))}
            label={
              (lead.score ?? (formSubmissions.length * 50 + landingEvents.length * 10 + (lead.isSubscribed ? 20 : 0))) >= 67
                ? "Hot"
                : (lead.score ?? 0) >= 34
                ? "Warm"
                : "Cold"
            }
            lastUpdated={lead.createdAt ?? "just now"}
          />
        )}
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-xl shadow-sm">
        <div className="border-b flex gap-6 px-6">
          {/* Tab Buttons Timeline, forms, Landing Pages, Emails, Notes and talking Points */}
          {["Timeline", "Forms", "Landing Pages", "Emails"].map((t) => (
            <button
              key={t}
              className={`py-3 text-sm font-medium ${
                tab === t.toLowerCase() ? "text-blue-600 border-b-2 border-blue-600" : "text-gray-500"
              }`}
              onClick={() => setTab(t.toLowerCase())}
            >
              {t}
            </button>
          ))}
        </div>

        {/* Activity List */}
        <div className="p-6">
          {filteredActivities.length === 0 ? (
            <EmptyState
              title="No activities yet"
              description="This lead has no recorded interactions. Check back later."
            />
          ) : (
            filteredActivities.map((a) => (
              <ActivityCard key={a.id} activity={a} timeAgo={timeAgo} />
            ))
          )}
        </div>
      </div>

      {/* Footer */}
      {/* <div className="bg-white rounded-xl shadow-sm p-6 mt-6 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <span>Status:</span>
          <select className="border rounded-md px-3 py-1 text-sm">
            <option>Contacted</option>
            <option>New</option>
            <option>Qualified</option>
          </select>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={handleSync}
            className="bg-blue-600 text-white text-sm px-4 py-2 rounded-md"
          >
            {syncing ? "Syncing..." : "Sync with CRM"}
          </button>
          <p className="text-xs text-gray-400">Last sync: {lastSync}</p>
        </div >
      </div> */}
    </div>
  );
};

export default LeadDetailsPage;
