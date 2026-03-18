import { Link } from "react-router-dom";
import Logo from "../assets/NavbarLogo.svg";

const LoginPage = () => {
  return (
    <div className="min-h-screen flex">
      <div className="w-5/12 bg-gradient-to-br from-blue-50 to-purple-50 flex flex-col justify-between p-16">
        <div>
          <div className="flex items-center mb-8">
            <div className="h-8 w-8 rounded-xl bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center mr-3">
              <img src={Logo} alt="Logo" className="h-4 w-4" />
            </div>
            <h1 className="text-xl font-semibold text-gray-800">Operimus</h1>
          </div>

          <div>
            <h2 className="text-3xl font-bold text-gray-800 mb-3">
              Automate smarter. <br /> Market better.
            </h2>
            <p className="text-gray-600 mb-6">
              Transform your marketing workflows with intelligent automation
              that drives results.
            </p>
            <ul className="text-gray-700 space-y-2">
              <li className="flex items-center gap-2">
                <span className="text-blue-500">✔</span> Advanced campaign
                management
              </li>
              <li className="flex items-center gap-2">
                <span className="text-blue-500">✔</span> Real-time analytics &
                insights
              </li>
              <li className="flex items-center gap-2">
                <span className="text-blue-500">✔</span> Seamless integrations
              </li>
            </ul>
          </div>
        </div>

        <footer className="text-gray-400 text-sm">
          © 2025 Operimus. All rights reserved.
        </footer>
      </div>

      {/* Right section */}
      <div className="w-7/12 flex items-center justify-center bg-white">
        <div className="max-w-md w-full px-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-2">
            Login to Operimus
          </h2>
          <p className="text-gray-500 mb-6">
            Enter your credentials to access your account.
          </p>

          <form className="space-y-5">
            <div>
              <label
                htmlFor="email"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Email or Username
              </label>
              <input
                type="email"
                id="email"
                placeholder="Enter your email or username"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>

            <div>
              <label
                htmlFor="password"
                className="block text-sm font-medium text-gray-700 mb-1"
              >
                Password
              </label>
              <input
                type="password"
                id="password"
                placeholder="Enter your password"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
              <div className="text-right mt-2">
                <Link
                  to="/forgot-password"
                  className="text-sm text-blue-600 hover:underline"
                >
                  Forgot password?
                </Link>
              </div>
            </div>

            <button
              type="button"
              className="w-full bg-blue-600 text-white py-2 rounded-lg font-medium hover:bg-blue-700 transition"
            >
              Login
            </button>
          </form>

          <p className="text-sm text-gray-600 text-center mt-6">
            Don’t have an account?{" "}
            <Link
              to="/signup"
              className="text-blue-600 font-medium hover:underline"
            >
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
