import { Navigate } from "react-router-dom";
import { useUser } from "../context/UserContext";
import LoadingState from "./states/LoadingState";
import type { JSX } from "react";
import keycloak from "../keycloak";

interface ProtectedRouteProps {
  children: JSX.Element;
  requiredRoles?: string[];
}

const ProtectedRoute = ({ children, requiredRoles }: ProtectedRouteProps) => {
  const user = useUser();

  if (!user) {
    return <LoadingState message="Loading user information..." />;
  }

  if (!user.isAuthenticated) {
    keycloak.login();
    return <LoadingState message="Redirecting to login..." />;
  }

  if (requiredRoles && requiredRoles.length > 0) {
    const hasRole = requiredRoles.some((role) => user.hasRole(role));
    if (!hasRole) {
      console.warn("Access denied. Missing required roles:", requiredRoles);
      return <Navigate to="/app/campaigns" replace />;
    }
  }

  return children;
};

export default ProtectedRoute;
