import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import SideBar from "./sidebars/SideBar";

const Layout = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <header>
        <Navbar />
      </header>

      <div className="flex flex-grow">
        <aside className="w-56 bg-gray-100">
          <SideBar />
        </aside>

        <main className="flex-grow">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
