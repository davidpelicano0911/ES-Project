import React from "react";
import ReactDOM from "react-dom/client";

import "./styles/surveyjs.css";

import App from "./App";
import "./index.css";
import keycloak from "./keycloak";
import { UserProvider } from "./context/UserContext";
import flagsmith from "flagsmith";
import { FlagsmithProvider } from "flagsmith/react";
import './observability/otel-frontend';

// Check if accessing public routes that don't require Keycloak
const pathname = window.location.pathname;
const isPublicRoute = (
  (pathname.startsWith("/forms/") && pathname.includes("/view")) ||
  (pathname.startsWith("/landingpages/") && pathname.includes("/view"))
);

const keycloakConfig = isPublicRoute 
  ? { onLoad: "check-sso" as const }  // Don't require login, but check for existing session
  : { onLoad: "login-required" as const };  // Require login for protected routes

keycloak.init(keycloakConfig).then((authenticated) => {
  // Setup for authenticated users
  if (authenticated) {
    console.log("Logged in as:", keycloak.tokenParsed?.preferred_username);

    setInterval(() => {
      keycloak.updateToken(60).catch(() => keycloak.login());
    }, 30000);

    const urlParams = new URLSearchParams(window.location.search);
    const forcedUser = urlParams.get("user"); // ex: ?user=A
    const identity =
      forcedUser ||
      keycloak.tokenParsed?.preferred_username ||
      `local_${Math.floor(Math.random() * 100000)}`;

    flagsmith.identify(identity);
    console.log("Flagsmith identity set to:", identity);
  } else {
    // Not authenticated
    console.warn("Not authenticated");
    
    // Only redirect to login if accessing protected routes
    // Public routes can be accessed without authentication
    if (!isPublicRoute) {
      keycloak.login();
      return; // Don't render app yet, wait for redirect
    }
  }
  
  // Render app (for authenticated users or public routes)
  ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
      <FlagsmithProvider
        options={{
          environmentID: import.meta.env.VITE_FLAGSMITH_ENV_ID,
        }}
        flagsmith={flagsmith}
      >
        <UserProvider>
          <App />
        </UserProvider>
      </FlagsmithProvider>
    </React.StrictMode>
  );
});