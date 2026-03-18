import { useState, useMemo } from "react";
import { NavLink, useLocation } from "react-router-dom";
import {
  Megaphone,
  FolderOpen,
  Mail,
  FileSpreadsheet,
  MessageSquare,
  FileText,
  FlaskConical,
  Workflow,
  Users,
  LayoutDashboard,
  BarChart3,
  ChevronDown,
  ChevronRight,
} from "lucide-react";

const SideBar = () => {
  const [isContentOpen, setIsContentOpen] = useState(true);
  const location = useLocation();

  const contentRoutes = [
    "/email-templates",
    "/landing-pages",
    "/social-posts",
    "/forms",
    "/tests-ab",
    "/workflow-templates",
  ];

  const isContentActive = useMemo(
    () => contentRoutes.some((route) => location.pathname.startsWith(route)),
    [location]
  );

  const linkClasses = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-2 px-4 py-2 rounded-md font-medium transition-all duration-150 ${
      isActive
        ? "bg-blue-600 text-white"
        : "text-gray-700 hover:bg-blue-50 hover:text-blue-700"
    }`;

  const iconClass = (isActive: boolean) =>
    `h-5 w-5 transition-colors duration-150 ${
      isActive ? "text-white" : "text-gray-600 group-hover:text-blue-700"
    }`;

  return (
    <div className="w-56 bg-white h-full p-4 flex flex-col relative z-40 shadow-[2px_0_6px_rgba(0,0,0,0.06)]">
      <NavLink to="/app/campaigns" className={linkClasses}>
        {({ isActive }) => (
          <>
            <Megaphone className={iconClass(isActive)} />
            <span>Campaigns</span>
          </>
        )}
      </NavLink>

      <div className="mt-4">
        <button
          onClick={() => setIsContentOpen(!isContentOpen)}
          className={`flex items-center justify-between w-full px-4 py-2 rounded-md font-medium cursor-pointer transition ${
            isContentActive
              ? "bg-blue-600 text-white"
              : "text-gray-700 hover:bg-blue-50 hover:text-blue-700"
          }`}
        >
          <span className="flex items-center gap-2">
            <FolderOpen
              className={`h-5 w-5 ${
                isContentActive ? "text-white" : "text-gray-600"
              }`}
            />
            Content
          </span>
          {isContentOpen ? (
            <ChevronDown
              className={`h-4 w-4 ${
                isContentActive ? "text-white" : "text-gray-600"
              }`}
            />
          ) : (
            <ChevronRight
              className={`h-4 w-4 ${
                isContentActive ? "text-white" : "text-gray-600"
              }`}
            />
          )}
        </button>

        {isContentOpen && (
          <div className="ml-6 mt-2 flex flex-col gap-2 text-gray-600">
            <NavLink to="/app/email-templates" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <Mail className={iconClass(isActive)} />
                  <span>Email Templates</span>
                </>
              )}
            </NavLink>

            <NavLink to="/app/landing-pages" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <FileSpreadsheet className={iconClass(isActive)} />
                  <span>Landing Pages</span>
                </>
              )}
            </NavLink>

            <NavLink to="/app/social-posts" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <MessageSquare className={iconClass(isActive)} />
                  <span>Social Posts</span>
                </>
              )}
            </NavLink>

            <NavLink to="/app/forms" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <FileText className={iconClass(isActive)} />
                  <span>Forms</span>
                </>
              )}
            </NavLink>

            <NavLink to="/app/tests-ab" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <FlaskConical className={iconClass(isActive)} />
                  <span>Tests A/B</span>
                </>
              )}
            </NavLink>

            <NavLink to="/app/workflow-templates" className={linkClasses}>
              {({ isActive }) => (
                <>
                  <Workflow className={iconClass(isActive)} />
                  <span>Workflow Templates</span>
                </>
              )}
            </NavLink>
          </div>
        )}
      </div>

      <div className="mt-4 flex flex-col gap-3 text-gray-700">
        <NavLink to="/app/leads" className={linkClasses}>
          {({ isActive }) => (
            <>
              <Users className={iconClass(isActive)} />
              <span>Leads</span>
            </>
          )}
        </NavLink>

        <NavLink to="/app/dashboard" className={linkClasses}>
          {({ isActive }) => (
            <>
              <LayoutDashboard className={iconClass(isActive)} />
              <span>Dashboard</span>
            </>
          )}
        </NavLink>

        <NavLink to="/app/reports" className={linkClasses}>
          {({ isActive }) => (
            <>
              <BarChart3 className={iconClass(isActive)} />
              <span>Reports</span>
            </>
          )}
        </NavLink>
      </div>
    </div>
  );
};

export default SideBar;
