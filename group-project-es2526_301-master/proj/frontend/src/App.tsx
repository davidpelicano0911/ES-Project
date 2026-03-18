// App.tsx
import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import Layout from "./components/Layout";
import CampaignPage from "./pages/campaigns/CampaignPage";
import EmailTemplatesPage from "./pages/emails/EmailTemplatesPage";
import LandingPagesPage from "./pages/landingpages/LandingPagesPage";
import SocialPostsPage from "./pages/socialposts/SocialPostsPage";
import FormsPage from "./pages/forms/FormsPage";
import TestsABPage from "./pages/emails/TestsABPage";
import WorkflowTemplatesPage from "./pages/workflows/WorkflowTemplatesPage";
import LeadsPage from "./pages/leads/LeadsPage";
import LeadDetailsPage from "./pages/leads/LeadDetailsPage";
import DashboardPage from "./pages/dashboards/DashboardPage";
import ReportsPage from "./pages/reports/ReportsPage";
import CampaignDetailsPage from "./pages/campaigns/CampaignDetailsPage";
import CreateCampaignPage from "./pages/campaigns/CreateCampaignPage";
import CreateWorkflowPage from "./pages/workflows/CreateWorkflowPage";
import CreateWorkflowTemplate from "./pages/workflows/CreateWorkflowTemplate";
import CreateCampaignDashboard from "./pages/dashboards/CreateCampaignDashboard";
import LoginPage from "./pages/LoginPage";
import ProtectedRoute from "./components/ProtectedRoute";
import WorkflowBuilder from "./pages/workflows/WorkflowBuilder";
import WorkflowBuilderTemplate from "./pages/workflows/WorkflowBuilderTemplate";
import CreateEmailTemplatePage from "./pages/emails/CreateEmailTemplatePage";
import EditDashboardPage from "./pages/dashboards/EditDashboardPage";
import CreateSocialPostsPage from "./pages/socialposts/CreateSocialPostsPage";
import FailMessage from "./components/messages/FailMessage";
import { clearGlobalError } from "./utils/globalError";
import { useEffect, useState } from "react";
import FormBuilderPage from "./pages/forms/FormsBuilderPage";
import CreateLandingPage from "./pages/landingpages/CreateLandingPage";
import PublicFormViewPage from "./pages/forms/PublicFormViewPage";
import PublicLandingPageViewPage from "./pages/landingpages/PublicLandingPageViewPage";

import EmailTemplateViewPage from "./pages/emails/EmailTemplateViewPage";
import FormTemplateViewPage from "./pages/forms/FormTemplateViewPage";
import LandingPageViewPage from "./pages/landingpages/LandingPageViewPage";
import SocialPostViewPage from "./pages/socialposts/SocialPostViewPage";
import DashboardDetailsPage from "./pages/dashboards/DashboardDetails";
import CampaignReportsPage from "./pages/reports/CampaignReportsPage";

import "./App.css";
import MyChat from "./components/MyChat";

function App() {
  console.log("API VERSION:", import.meta.env.VITE_API_VERSION);
  const [globalErrorMessage, setGlobalErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const onError = (e: Event) => {
      const ev = e as CustomEvent;
      setGlobalErrorMessage(ev.detail?.message || "An error occurred");
    };

    const onClear = () => setGlobalErrorMessage(null);

    window.addEventListener("global-error", onError as EventListener);
    window.addEventListener("global-error-clear", onClear as EventListener);

    return () => {
      window.removeEventListener("global-error", onError as EventListener);
      window.removeEventListener("global-error-clear", onClear as EventListener);
    };
  }, []);

  return (
    <Router>
      {globalErrorMessage && (
        <FailMessage
          entity={globalErrorMessage}
          onClose={() => {
            setGlobalErrorMessage(null);
            clearGlobalError();
          }}
        />
      )}

      <Routes>
        <Route path="/" element={<Navigate to="/app/campaigns" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route path="campaigns" element={<CampaignPage />} />
          <Route path="email-templates" element={<EmailTemplatesPage />} />
          <Route path="landing-pages" element={<LandingPagesPage />} />
          <Route path="social-posts" element={<SocialPostsPage />} />
          <Route path="forms" element={<FormsPage />} />
          <Route path="tests-ab" element={<TestsABPage />} />
          <Route path="campaigns/:id" element={<CampaignDetailsPage />} />
          <Route path="workflow-templates" element={<WorkflowTemplatesPage />} />
          <Route path="leads" element={<LeadsPage />} />
          <Route path="leads/:id" element={<LeadDetailsPage />} />
          <Route
            path="email-templates/create"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <CreateEmailTemplatePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="email-templates/edit/:id"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <CreateEmailTemplatePage />
              </ProtectedRoute>
            }
          />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="reports" element={<ReportsPage />} />

          <Route path="reports/:id" element={<CampaignReportsPage />} />
          <Route
            path="campaigns/create"
            element={
              <ProtectedRoute requiredRoles={["MARKETING_MANAGER"]}>
                <CreateCampaignPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="workflows/create"
            element={
              <ProtectedRoute requiredRoles={["MARKETING_MANAGER"]}>
                <CreateWorkflowPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="workflow-templates/create"
            element={
              <ProtectedRoute requiredRoles={["MARKETING_MANAGER"]}>
                <CreateWorkflowTemplate />
              </ProtectedRoute>
            }
          />
          <Route
            path="social-posts/create"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <CreateSocialPostsPage />
              </ProtectedRoute>
            }
          />
          <Route path="workflow-templates/:id" element={<WorkflowBuilderTemplate />} />
          <Route path="workflows/:id" element={<WorkflowBuilder />} />
          <Route
            path="dashboard/create"
            element={
              <ProtectedRoute requiredRoles={["MARKETING_ANALYST"]}>
                <CreateCampaignDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="dashboard/edit/:id?"
            element={
              <ProtectedRoute requiredRoles={["MARKETING_ANALYST"]}>
                <EditDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route path="dashboard/view/:id" element={<DashboardDetailsPage />} />
          <Route
            path="forms/builder"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <FormBuilderPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="forms/builder/:id"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <FormBuilderPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="landing-pages/create"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <CreateLandingPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="landing-pages/edit/:id"
            element={
              <ProtectedRoute requiredRoles={["CONTENT_MARKETER"]}>
                <CreateLandingPage />
              </ProtectedRoute>
            }
          />

          <Route path="email-templates/view/:id" element={<EmailTemplateViewPage />} />

          <Route path="forms/view/:id" element={<FormTemplateViewPage />} />

          <Route path="landing-pages/view/:id" element={<LandingPageViewPage />} />

          <Route path="social-posts/view/:id" element={<SocialPostViewPage />} />


          
        </Route>

        {/* Public Routes - Accessible to all users without authentication */}
        <Route
          path="/forms/:id/view"
          element={<PublicFormViewPage />}
        />
        <Route
          path="/landingpages/:id/view"
          element={<PublicLandingPageViewPage />}
        />
      </Routes>

      <MyChat />
    </Router>
  );
}

export default App;
