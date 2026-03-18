// src/utils/auth.ts
import keycloak from "../keycloak";

export const hasRole = (role: string): boolean => {
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  return roles.includes(role);
};
