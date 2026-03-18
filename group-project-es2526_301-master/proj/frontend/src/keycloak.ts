import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: "marketing-realm",
  clientId: "marketing-frontend",
});

(window as any).keycloak = keycloak;

export default keycloak;
