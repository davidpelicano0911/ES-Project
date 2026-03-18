import { Link } from "react-router-dom";
import Logo from "../assets/NavbarLogo.svg";
import { User } from "lucide-react";
import keycloak from "../keycloak";

const Navbar = () => {
  const username =
    keycloak.tokenParsed?.name ||
    keycloak.tokenParsed?.preferred_username ||
    "User";

  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  const role =
    roles.length > 0 ? roles[0].replaceAll("_", " ").toLowerCase() : "no role";

  const handleLogout = () => {
    keycloak.logout({ redirectUri: window.location.origin });
  };

  return (
    <div className="navbar bg-base-100 shadow-sm h-12 px-16 z-1000">
      <div className="flex-1 flex items-center">
        <div className="h-8 w-8 rounded-xl bg-linear-to-br from-blue-500 to-purple-500 flex items-center justify-center mr-3">
          <img src={Logo} alt="Logo" className="h-4 w-4" />
        </div>
        <Link to="/app/campaigns" className="text-lg font-bold text-gray-800">
          Operimus
        </Link>
      </div>

      <div className="flex items-center gap-3">
        <div className="flex flex-col items-end leading-tight mr-1">
          <span className="text-sm font-medium text-gray-800">{username}</span>
          <span className="text-xs text-gray-500 capitalize">{role}</span>
        </div>

        <div className="p-2 bg-gray-200 rounded-full flex items-center justify-center">
          <User className="h-5 w-5 text-gray-600" />
        </div>

        <button
          onClick={handleLogout}
          className="text-gray-700 hover:text-gray-900 text-sm font-medium cursor-pointer transition hover:underline"
        >
          Sign Out
        </button>
      </div>
    </div>
  );
};

export default Navbar;
