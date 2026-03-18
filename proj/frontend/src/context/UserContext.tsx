import { createContext, useContext, useState, useEffect } from "react";
import LoadingState from "../components/states/LoadingState";
import type { ReactNode } from "react";
import keycloak from "../keycloak";

interface UserContextType {
  name: string;
  email: string;
  roles: string[];
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
  logout: () => void;
}

export const UserContext = createContext<UserContextType | null>(null);

export const UserProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<UserContextType | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (keycloak.authenticated && keycloak.tokenParsed) {
      const parsed = keycloak.tokenParsed as any;
      const roles = parsed.realm_access?.roles ?? [];

      setUser({
        name: parsed.name ?? parsed.preferred_username ?? "User",
        email: parsed.email ?? "",
        roles,
        isAuthenticated: true,
        hasRole: (role: string) => roles.includes(role),
        logout: () => keycloak.logout({ redirectUri: window.location.origin }),
      });
    } else {
      setUser(null);
    }

    setLoading(false);
  }, []);

  if (loading) {
    return <LoadingState message="Authenticating user..." />;
  }

  return <UserContext.Provider value={user}>{children}</UserContext.Provider>;
};

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) throw new Error("useUser must be used within a UserProvider");
  return context;
};
