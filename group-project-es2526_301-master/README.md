
# Marketing Automation Platform

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](backend/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=flat&logo=spring&logoColor=white)](backend/)
[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat&logo=react&logoColor=black)](frontend/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%20%2B%20pgvector-336791?style=flat&logo=postgresql&logoColor=white)](https://github.com/pgvector/pgvector)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)](proj/)
[![Terraform](https://img.shields.io/badge/Terraform-1.13-7B42BC?style=flat&logo=terraform&logoColor=white)](proj/)

An enterprise-grade REST API and frontend service for managing marketing campaigns, email automation, and customer segmentation. Features **Spring AI** integration with **Google Gemini** for semantic search and content generation.

---

## Documentation Navigation

This repository is organized into a mono-repo structure. Please refer to the specific directories for detailed developer documentation:

| Component | Path | Description |
|-----------|------|-------------|
| **Backend** | [`proj/backend/README.md`](proj/backend/README.md) | API Endpoints, Spring Boot Config, Testing strategies. |
| **Frontend** | [`proj/frontend/README.md`](proj/frontend/README.md) | React components, Vite setup, UI architecture. |
| **Infrastructure** | [`proj/`](proj/) | Terraform scripts, Docker Compose, and Env configurations. |
| **Architecture** | [`docs/`](docs/) | Domain models, System diagrams, and ADRs. |

---

## System Architecture

The platform follows a **Microservices-ready** architecture using Docker containers orchestrated via Terraform.

*   **Frontend:** React (Vite) Single Page Application.
*   **Backend:** Spring Boot Resource Server.
*   **Auth:** Keycloak (OAuth2/OIDC).
*   **Database:** PostgreSQL 14 (with `pgvector` extension).
*   **Observability:** OpenTelemetry Collector + Prometheus + Grafana.

![System Architecture](docs/architecture/architecture.png)

---

## Quick Start

### Prerequisites

*   **Docker** & **Docker Compose**
*   **Terraform**
*   **Java 17+** (Optional, for local dev without Docker)

### 1. Environment Setup

The infrastructure requires environment variables.
Create .env and terraform.tfvars with actual values check [`docs/EnvironmentSetup.md`](docs/EnvironmentSetup.md) for details.

### 2. Launch the Stack

We use Terraform to provision the local Docker infrastructure (Database, Keycloak, Backend, Frontend).

```bash
cd proj/
terraform init
terraform apply -auto-approve
```

### 3. Access Services

Once the containers are running, access the services at:

| Service | URL | Default Creds (Dev) |
|---------|-----|---------------------|
| **Frontend** | `http://localhost:3000` | N/A |
| **Backend API** | `http://localhost:8080` | [Swagger UI](http://localhost:8080/swagger-ui/index.html) |
| **Keycloak** | `http://localhost:8081` | `admin` / `admin` |

---

## 🌍 Deployment & Remote Configuration

**⚠️ CRITICAL: Read this if deploying to AWS, Azure, or a VPS.**

The default configuration is set to `localhost`. If you deploy this to a remote server, the OAuth2 authentication flows (Keycloak) and CORS policies will fail unless you update the host configuration.

**Before running `terraform apply` on a remote server:**

1.  **Run the IP Configuration Script:**
    This script updates `terraform.tfvars` and `.env`, Keycloak redirects, and frontend environment files to match your public IP or Domain.

    ```bash
    # From project root
    chmod +x proj/setup-machine-ip.sh
    ./proj/setup-machine-ip.sh
    ```

2.  **Deploy:**
    ```bash
    cd proj/
    terraform apply -auto-approve
    ```

---

## 🛠️ Development Workflow

[`docs/gitworkflow/git-flow.md`](docs/gitworkflow/git-flow.md) outlines our branching strategy, commit conventions, and PR process.

### Troubleshooting

* **Keycloak Loop:** Ensure you ran `./setup-machine-ip.sh` if not on localhost.
* **Environments variables:** Verify all required `.env` and `terraform.tfvars` vars are set.

---

## Contributors

- David Pelicano - Scrum Master
- Guilherme Alves - Product Owner
- Henrique Ferreira - DevOps Engineer
- João Monteiro - Service Analyst
- Jorge Domingues - QA Engineer