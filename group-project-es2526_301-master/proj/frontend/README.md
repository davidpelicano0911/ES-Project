# Frontend Overview

This document provides an overview of the **frontend architecture** of the project — covering the technologies used, rationale behind their selection, key dependencies, folder structure and development setup.

---

## Technology Stack and Rationale

The frontend was built using **React + TypeScript + Vite**, ensuring high performance, scalability, and maintainability.

### 1. React

React was chosen for its **component-based architecture**, which allows modular and reusable UI design. Its virtual DOM provides efficient rendering, and its ecosystem supports fast development and integration.

**Advantages:**
- Declarative and modular components.
- Strong community and ecosystem support.
- Seamless API and backend integration.

### 2. TypeScript

TypeScript adds **static typing** to JavaScript, improving reliability and readability while reducing runtime errors.

**Advantages:**
- Type safety and improved IDE support.
- Easier debugging and refactoring.
- Enhanced scalability for larger applications.

### 3. Vite

Vite was selected for its **modern development performance**, enabling instant hot module replacement (HMR) and optimized builds.

**Advantages:**
- Lightning-fast dev server startup.
- ES module–based optimized builds.
- Simple configuration and cleaner workflow.

### 4. Tailwind CSS & DaisyUI

Tailwind CSS uses a **utility-first** approach, enabling rapid UI development directly in the markup. DaisyUI extends Tailwind with pre-built, customizable components for consistent and modern designs.

**Advantages:**
- Rapid prototyping and consistent design.
- Fully responsive layouts.
- Built-in theming with accessibility in mind.

### 5. ESLint & Prettier

To maintain code quality and consistency, **ESLint** handles linting and **Prettier** ensures automatic formatting.

**Advantages:**
- Enforced code standards across contributors.
- Reduces syntax and style-related bugs.
- Clean and readable codebase.

---

## Key Dependencies & Libraries

Beyond the core stack, the application leverages powerful libraries to handle complex features efficiently.

### AI & Chat
- **Vercel AI SDK (ai, @ai-sdk/openai):** Handles the integration with LLMs (OpenAI) for streaming responses and chat capabilities.
- **Assistant UI (@assistant-ui/react):** Provides pre-built, accessible UI components specifically for AI chat interfaces.

### Specialized Editors & Builders
- **XYFlow (@xyflow/react):** Formerly React Flow. Powering the interactive **Workflow Builder**, allowing users to drag-and-drop nodes to create automation flows.
- **SurveyJS (survey-core, survey-creator-react):** A robust engine used for the **Form Builder** and rendering surveys/forms dynamically.
- **React Email Editor (react-email-editor):** Embeds a drag-and-drop email design tool directly into the application.

### Visualization & Dashboards
- **Recharts & Chart.js:** Used for rendering analytics charts (bar, line, pie) in the Dashboard and Reports pages.
- **React Grid Layout:** Enables the draggable and resizable widget grid system on the Dashboard.

### Infrastructure & Utils
- **Keycloak JS:** Manages OpenID Connect (OIDC) authentication and session management.
- **OpenTelemetry (@opentelemetry/*):** Handles frontend observability, distributed tracing, and performance monitoring.
- **Flagsmith:** Used for Feature Flag management (toggling features on/off remotely).
- **Axios:** Standard HTTP client for making API requests.

---

## Project Structure

```
frontend/
├── node_modules/
├── public/
├── src/
│   ├── api/                     # Centralized API modules (REST endpoints integration)
│   │   ├── apiCampaigns.tsx
│   │   ├── apiChat.tsx
│   │   ├── apiConfig.tsx
│   │   ├── apiDashboards.tsx
│   │   ├── apiEmailLogs.tsx
│   │   ├── apiEmailTemplates.tsx
│   │   ├── apiFormBuilder.tsx
│   │   ├── apiFormSubmissions.tsx
│   │   ├── apiLandingPageEvents.tsx
│   │   ├── apiLandingPages.tsx
│   │   ├── apiLeads.tsx
│   │   ├── apiMaterials.tsx
│   │   ├── apiPosts.tsx
│   │   ├── apiSegments.tsx
│   │   ├── apiWorkflows.tsx
│   │   └── apiWorkflowTemplates.tsx
│   │
│   ├── assets/                  # Static assets such as images, icons, etc.
│   │
│   ├── components/              # Reusable UI components
│   │   ├── alerts/              # Alert components
│   │   ├── buttons/             # Button components
│   │   ├── cards/               # Card widgets and post cards
│   │   ├── graphics/            # Graphs and visual elements
│   │   ├── messages/            # Toasts, alerts, and notifications
│   │   ├── modals/              # Popup modals for CRUD or previews
│   │   ├── searchandfilters/    # Search bars and filtering components
│   │   ├── sidebars/            # Sidebar components
│   │   └── states/              # UI state indicators and placeholders
│   │
│   ├── context/                 # React Contexts (e.g. UserContext)
│   │   └── UserContext.tsx
│   │
│   ├── hooks/                   # Custom reusable React hooks
│   │   └── useFormTemplates.ts
│   │
│   ├── observability/           # Observability files
│   │   └── otel-frontend.ts
│   │
│   ├── pages/                   # Main pages/routes of the app
│   │   ├── campaigns/
│   │   ├── dashboards/
│   │   ├── emails/
│   │   ├── forms/
│   │   ├── landingpages/
│   │   ├── leads/
│   │   ├── reports/
│   │   ├── socialposts/
│   │   ├── workflows/
│   │   ├── CreateFormTemplatePage.tsx
│   │   └── LoginPage.tsx
│   │
│   ├── types/                   # TypeScript type definitions
│   │   ├── activity.ts
│   │   ├── campaign.ts
│   │   ├── dashboard.ts
│   │   ├── emailLog.ts
│   │   ├── emailTemplate.ts
│   │   ├── formTemplate.ts
│   │   ├── landingPage.ts
│   │   ├── landingPageEvent.ts
│   │   ├── lead.ts
│   │   ├── post.ts
│   │   ├── postDTO.ts
│   │   ├── segment.ts
│   │   ├── survey.d.ts
│   │   └── workflow.ts
│   │
│   ├── utils/                   # Helper utilities (e.g., auth, icons)
│   │   ├── auth.ts
│   │   ├── getTemplateIcon.ts
│   │   ├── globalError.ts
│   │   ├── materialMappers.tsx
│   │   └── trackingMetadata.ts
│   │
│   ├── styles/                  # Tailwind and global CSS styles
│   │
│   ├── App.tsx                  # Root React component
│   ├── App.css                  # Global application styles
│   ├── index.css                # Base Tailwind & reset styles
│   ├── keycloak.ts              # Keycloak initialization and configuration
│   └── main.tsx                 # Application entry point
│
├── keycloak/                    # Keycloak configuration and realms
│   └── realms/
│       └── marketing-realm.json
│
├── nginx.conf                   # Custom Nginx configuration for static hosting
├── .gitignore
├── Dockerfile                   # Container definition for frontend
├── eslint.config.js             # Linting configuration
├── index.html                   # Base HTML template
├── package.json
├── package-lock.json
├── tailwind.config.ts
├── tsconfig.app.json
├── tsconfig.node.json
├── tsconfig.json
└── vite.config.ts               # Vite configuration for builds and dev server
```

---

## Development Setup

### Prerequisites

- **Node.js** ≥ 20.0
- **npm** (or **yarn**)

### Installation

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies
npm install
```

### Available Scripts

| Command | Description |
| :--- | :--- |
| `npm run dev` | Starts the development server with HMR. |
| `npm run build` | Builds the app for production to the `dist` folder. |
| `npm run preview` | Locally preview the production build. |
| `npm run lint` | Runs ESLint to check for code quality issues. |

### Run Development Server

```bash
npm run dev
```

Once running, access the application at:
`http://localhost:5173`

---

## Build for Production

```bash
npm run build
```

This command generates an optimized production build inside the `dist/` folder, ready for deployment.

To run the full application, including both the API (backend) and the frontend, navigate back to the project root directory and run:

```bash
/frontend$ cd ..
/proj$ terraform apply -auto-approve
```

Once running, access the application at:
`http://localhost:3000`

---

## Contribution Guidelines

To ensure consistency, please follow these conventions:

1.  **Components:** Place reusable UI elements in `src/components`. Small, specific components are preferred over large monolithic ones.
2.  **Pages:** Screens that correspond to a route go into `src/pages`.
3.  **API Calls:** Do not make `fetch/axios` calls directly inside components. Create a function in `src/api/` and import it.
4.  **Styling:** Use Tailwind utility classes. If a component becomes too complex, extract it or use DaisyUI classes.
5.  **Types:** Always define interfaces in `src/types/` instead of using `any`.