# Marketing Platform Backend

REST API service for the Marketing Platform, handling campaign management, email automation, customer segmentation, and workflow orchestration with AI-powered features.

---

## Tech Stack

- **Framework:** Spring Boot 3.5.6 (Java 17+)
- **Database:** PostgreSQL 14 with pgvector extension
- **Security:** OAuth2 Resource Server (JWT) via Keycloak
- **AI:** Spring AI with Google Gemini (embeddings & chat)
- **Observability:** OpenTelemetry, Prometheus metrics, distributed tracing
- **Build:** Maven
- **Containerization:** Docker, docker-compose, Terraform

---

## Quick Start (Recommended)

Get the full stack running in under 5 minutes using Docker Compose:

```bash
cd proj/
terraform apply -auto-approve
```

**Services:**

- Backend: `http://localhost:8080`
- Frontend: `http://localhost:3000`
- Keycloak: `http://localhost:8081`
- PostgreSQL: `localhost:5432`

**Note:** Ensure `terraform.tfvars` and `.env` exist in `proj/` with required environment variables (see Configuration section below). 

---

## Prerequisites

Before running locally, install:

- **Java 17+** (Temurin/OpenJDK recommended)
- **Docker** 
- **Maven** 
- **Terraform** 

Verify installations:

```bash
java -version    # Should show Java 17 or higher
docker --version # Docker Engine 20.10+
mvn -v          
terraform -v    
```

---

## Configuration

### Environment Variables

The backend requires specific environment variables. These are configured in `proj/terraform.tfvars`.

**Required (Core Application):**

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` or `dev` |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Keycloak realm issuer URI | `http://localhost:8081/realms/marketing` |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | Keycloak JWK Set URI | `http://localhost:8081/realms/marketing/protocol/openid-connect/certs` |
| `POSTGRES_USER` | Database username | `username` |
| `POSTGRES_PASSWORD` | Database password | `password` |
| `POSTGRES_DB` | Database name | `app_db` |
| `BACKEND_DOCKER_PORT` | Backend exposed port | `8080` |
| `MACHINE_URL` | Public machine URL | `localhost` |

**Required (AI & APIs):**

| Variable | Description | Required For |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini API key for AI features | AI embeddings & chat |
| `GOOGLE_PROJECT_ID` | Google Cloud project ID | AI embeddings & chat |
| `MAILJET_API_USERNAME` | Mailjet API username | Email sending |
| `MAILJET_API_PASSWORD` | Mailjet API password | Email sending |
| `FACEBOOK_ACCESS_TOKEN` | Facebook Graph API access token | Facebook Ads integration |
| `FACEBOOK_APP_ID` | Facebook App ID | Facebook Ads integration |
| `FACEBOOK_APP_SECRET` | Facebook App Secret | Facebook Ads integration |
| `FACEBOOK_PAGE_ID` | Facebook Page ID | Facebook Ads integration |
| `FACEBOOK_PUBLIC_PAGE_ID` | Facebook Public Page ID | Facebook Ads integration |
| `HUBSPOT_ACCESS_TOKEN` | HubSpot API access token | HubSpot CRM integration |

**Required (Observability):**

| Variable | Description | Example |
|----------|-------------|---------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OpenTelemetry collector endpoint | `http://localhost:4318/v1/traces` |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | OTLP protocol | `http/protobuf` |
| `OTEL_EXPORTER_OTLP_HEADERS` | OTLP authentication headers | `Authorization=ApiKey your-key` |
| `OTEL_SERVICE_NAME` | Service name for tracing | `Operimus` |
| `OTEL_RESOURCE_ATTRIBUTES` | Resource attributes | `service.version=1.0.0,deployment.environment=prod` |
| `OTEL_METRICS_EXPORTER` | Metrics exporter type | `otlp` |
| `OTEL_LOGS_EXPORTER` | Logs exporter type | `otlp` |

**Optional (Frontend):**

| Variable | Description | Required For |
|----------|-------------|--------------|
| `VITE_API_VERSION` | API version for frontend | Frontend configuration |

---

## Installation Commands

To install dependencies, run:

```bash# From backend/ directory
./mvnw clean dependency:resolve
```

## Database

### Development Database

The backend uses **PostgreSQL 14** with the `pgvector` extension for semantic search capabilities.

**Auto-initialization on startup:**

- Schema created via `spring.jpa.hibernate.ddl-auto=update`
- Seed data loaded from `src/main/resources/data.sql`:

**Connection details:**

- URL: `jdbc:postgresql://localhost:5432/operimusdb`
- Credentials: See `POSTGRES_*` environment variables

### Test Database

Integration tests use **H2 in-memory database** for isolation:

- Automatically created/destroyed per test run
- Profile: `test` (activated in `application-test.properties`)
- No seed data required

---

## Running the Application

```bash
cd proj/
terraform apply -auto-approve
```

## Running Tests

### Unit Tests

```bash
./mvnw clean test
```

### Integration Tests (Requires Docker)

```bash
./mvnw verify
```

Includes Cucumber E2E tests with Selenium WebDriver.

### Test Reports

View Allure test reports:

```bash
./mvnw allure:serve
```

---

## API Documentation

The backend exposes a REST API with 8 resource groups and 50+ endpoints:

- **Campaigns** - Campaign CRUD, status filtering, dashboards
- **Segments** - Customer segmentation management
- **Email Templates** - Template creation and testing
- **Leads** - Lead management
- **Workflows** - Workflow orchestration with nodes and edges
- **Form Templates** - Form builder and management
- **Landing Pages** - Landing page management
- **Public Endpoints** - Form submissions, health checks

**Base URL:** `http://localhost:8080`

**Authentication:** All non-`/public/**` endpoints require a Bearer token in the `Authorization` header.

### Swagger UI

Interactive API documentation and testing interface:

```text
http://localhost:8080/swagger-ui/index.html
```

**Swagger Authentication:**

- **client_id:** `marketing-backend`
- **client_secret:** `my-secret`

Use the "Authorize" button in Swagger UI to obtain a JWT token from Keycloak.

---

## Project Structure

```text
backend/
├─ src/
│  ├─ main/java/com/operimus/Marketing/
│  │  ├─ MarketingApplication.java          # Main entry poin
│  │  ├─ auth/SecurityConfig.java           # OAuth2 + CORS configuration
│  │  ├─ config/                              # Application configuration classes
│  │  ├─ component/                         # Spring components
│  │  ├─ controllers/                       # REST API endpoints
│  │  ├─ dto/                               # Data Transfer Objects
│  │  ├─ entities/                          # JPA entities (Campaign, Segment, etc.)
│  │  ├─ init/                              # Index Initialization
│  │  ├─ repositories/                      # Spring Data JPA repositories
│  │  ├─ security/                          # Security utilities
│  │  └─ services/                          # Business logic layer
|  |  └─ tool/MarketingTools.java            # AI integration utilities                            
│  └─ main/resources/
│     ├─ application.properties             # Main configuration
│     ├─ application-test.properties        # Test profile (H2 database)
│     └─ data.sql                           # Database seed data
├─ pom.xml                                  # Maven dependencies
└─ Dockerfile                               # Container image definition
```

---

## Key Business Logic

The backend implements several core business capabilities organized into service layers.


### 1. Workflow Engine ([`WorkflowEngine`](./src/main/java/com/operimus/Marketing/services/WorkflowEngine.java))

The central component of the marketing automation system. Responsible for orchestrating event-driven workflow execution.

#### Core Responsibilities

- **Event Processing**  
  Listens for events (form submissions, email interactions, landing page visits) and routes them to the appropriate workflows.
- **Node Execution**  
  Dynamically dispatches workflow nodes to registered handlers based on node type.
- **Lead Routing**  
  Matches leads to workflows based on segment membership and campaign status.
- **Instance Management**  
  Creates and tracks `WorkflowInstance` records representing each lead’s progression through a workflow.

#### Handler Pattern

Implements a Strategy-based architecture with specialized `NodeHandler` implementations:

- `SendEmailHandler` – Sends emails via MailJet  
- `DelayActionHandler` – Schedules delayed workflow actions  
- `AddToSegmentHandler` / `RemoveFromSegmentHandler` – Updates dynamic segment membership  
- `OnFormSubmittedTriggerHandler` – Triggers workflows based on form submission events


### 2. AI-Powered RAG System ([`MarketingIndexerService`](./src/main/java/com/operimus/Marketing/services/MarketingRAGService.java))

Implements Retrieval-Augmented Generation using Spring AI and Google Gemini to power marketing intelligence and conversational assistance.

#### Core Capabilities

- **Semantic Search**  
  Uses pgvector to retrieve relevant entities (campaigns, templates, landing pages, etc.).
- **Context-Aware Responses**  
  Injects retrieved documents into model prompts for accurate, company-specific answers.
- **Interactive Tool Execution**  
  Supports multi-turn interactions (e.g., completing campaign creation when parameters are missing).
- **Memory Management**  
  Maintains conversation context with `ChatMemory` for coherent follow-up queries.
- **Tool Integration**  
  Connects to `MarketingTools` to perform CRUD operations on campaigns, segments, workflows, and other marketing entities.

#### Key Features

- Filter-based search by entity type (EMAIL_TEMPLATE, CAMPAIGN, LANDING_PAGE, etc.)
- Automatic content indexing when entities change
- Confirmation loops for destructive operations (update/delete)


### 3. Vector Indexing Service ([`MarketingIndexerService`](src/main/java/com/operimus/Marketing/services/MarketingIndexerService.java))

Responsible for ingesting and maintaining the semantic search index backed by pgvector.

#### Auto-Indexed Entity Types

- Email Templates (subject + body)
- Landing Pages (name + HTML content)
- Campaigns (name + description + status)
- Workflows (name + description)
- Form Templates (name + description)
- Posts (content + platform metadata)
- Leads (name, email, score, segment information)

### Features

- **Auto-Indexing**  
  Automatically indexes entities on creation or update.
- **Metadata Enrichment**  
  Adds structured metadata (type, id, name) to support accurate filtering.
- **HTML Cleanup**  
  Strips HTML tags to generate higher-quality embeddings.


## Scheduled Services

- **Delay Scheduler (`DelaySchedulerService.java`)**  
  Executes delayed workflow actions at their scheduled timestamps.

- **Post Scheduler (`PostScheduler.java`)**  
  Publishes scheduled content to social media platforms.

- **MailJet Polling (`MailJetPollingService.java`)**  
  Polls MailJet every 2 minutes to ingest email engagement events.

---



## Architecture & Documentation

The backend follows **Domain-Driven Design** with a layered architecture:

- **Domain Model:** See [Domain Model Diagram](../../docs/domain_model/domainModelES_MAP.png)
- **System Architecture:** See [Backend Architecture Diagram](../../docs/architecture/architecture_MAP.png)

**Layers:**

1. **Controllers** - HTTP request handling, validation
2. **Services** - Business logic, transactions
3. **Repositories** - Data access (Spring Data JPA)
4. **Entities** - Domain model (JPA entities)

---

## CI/CD & Deployment

### GitHub Actions Workflows

- **PR Validation** (`.github/workflows/ci_docker_compose_and_tests.yml`) - Runs tests on pull requests
- **Production Deploy** (`.github/workflows/deploy.yml`) - Deploys to production
- **Test Reporting** (`.github/workflows/tests_and_allure_report.yml`) - Generates Allure test reports

### Terraform Deployment

Infrastructure provisioned via Terraform:

```bash
# From proj/ directory
terraform init
terraform plan
terraform apply -auto-approve
```

Provisions:

- PostgreSQL database with pgvector
- Backend container with OpenTelemetry agent
- Keycloak for authentication
- Observability stack

---

## Contributing

1. Create a feature branch from `dev`
2. Make changes and add tests
3. Submit pull request to `dev` branch
4. CI will run tests