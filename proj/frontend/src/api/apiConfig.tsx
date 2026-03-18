import axios from "axios";
import keycloak from "../keycloak";

const API_VERSION = import.meta.env.VITE_API_VERSION || "v1";
const BACKEND_HOST = import.meta.env.VITE_BACKEND_HOST || "http://localhost:8080";

const api = axios.create({
  baseURL: `${BACKEND_HOST}/api/${API_VERSION}`,
});

api.interceptors.request.use(async (config) => {
  const isPublicEndpoint = (() => {
    const url = config.url || "";
    // matches '/public', '/public/', '/public-' and '/public?'
    if (/\/public(?=$|\/|-|\?)/.test(url) || url.includes('/public-')) return true;

    // Allow public events POSTs (landing-page analytics)
    const method = (config.method || '').toLowerCase();
    if (method === 'post' && /\/events(?=$|\/|\?)/.test(url)) return true;

    return false;
  })();
  
  if (!isPublicEndpoint) {
    if (keycloak.isTokenExpired()) {
      await keycloak.updateToken(30);
    }

    const token = keycloak.token;
    console.log("Current Keycloak Token:", token);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }

  return config;
});

// Response interceptor: map network/backend errors to a global error event
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // If there's no response, it's a network/backend issue (server down, CORS or DNS)
    if (!error.response) {
      // Lazy import to avoid circular deps; don't block the rejection
      import("../utils/globalError")
        .then((mod) => {
          if (mod && typeof mod.showGlobalError === "function") {
            mod.showGlobalError("Backend not responding. Please try again later.");
          }
        })
        .catch(() => {
          /* ignore */
        });
    }
    return Promise.reject(error);
  }
);

export default api;
