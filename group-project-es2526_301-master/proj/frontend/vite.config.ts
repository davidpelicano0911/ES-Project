import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  optimizeDeps: {
    include: [
      "survey-react-ui",
      "survey-core",
      "survey-creator-core",
      "survey-creator-react",
    ],
  },
  server: {
    host: true,
    allowedHosts: ["deti-engsoft-11", "192.168.160.11", "localhost"],
    port: 5173,
  },
});
