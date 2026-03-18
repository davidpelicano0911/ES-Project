# Environment Setup Guide

This document explains all environmental variables used in the project, clearly separated between:

- **`.env`** - Used for Docker Compose local development
- **`terraform.tfvars`** - Used for Terraform infrastructure provisioning

---

### **Keycloak Configuration** (Authentication Server)
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `KEYCLOAK_IMAGE` | Docker image version for Keycloak authentication server | `quay.io/keycloak/keycloak:24.0.2` |
| `KEYCLOAK_ADMIN` | Admin username for Keycloak console | `your_admin_username` |
| `KEYCLOAK_ADMIN_PASSWORD` | Admin password for Keycloak console | `your_secure_password` |
| `KEYCLOAK_LOCAL_PORT` | Host machine port to access Keycloak | `8081` |
| `KEYCLOAK_DOCKER_PORT` | Internal Docker container port for Keycloak | `8080` |
| `KEYCLOAK_REALM` | Authentication realm name | `your-realm-name` |
| `KEYCLOAK_IMPORT_PATH` | Path to realm configuration files for import | `./keycloak/realms` |
| `KC_HOSTNAME_URL` | Public URL for Keycloak server | `http://localhost:8081` |

### **Spring Backend Security**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring Boot profile (prod/dev/test) | `prod` |
| `SPRING_SECURITY_ISSUER_URI` | OAuth2 token issuer URL for JWT validation | `http://localhost:8081/realms/your-realm` |
| `SPRING_SECURITY_JWK_URI` | JSON Web Key Set URL for token signature verification | `http://keycloak:8080/realms/your-realm/protocol/openid-connect/certs` |

### **Backend Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `BACKEND_LOCAL_PORT` | Host machine port to access backend API | `8080` |
| `BACKEND_DOCKER_PORT` | Internal Docker container port for backend | `8080` |

### **PostgreSQL Database**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `POSTGRES_DB` | Database name | `your_database_name` |
| `POSTGRES_USER` | Database username | `your_db_username` |
| `POSTGRES_PASSWORD` | Database password | `your_secure_db_password` |
| `PG_LOCAL_PORT` | Host machine port to access PostgreSQL | `5433` |
| `PG_DOCKER_PORT` | Internal Docker container port for PostgreSQL | `5432` |

### **Frontend Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `FRONTEND_LOCAL_PORT` | Host machine port to access frontend | `5173` |
| `FRONTEND_DOCKER_PORT` | Internal Docker container port for frontend | `5173` |
| `VITE_BACKEND_HOST` | Backend API URL for frontend to connect to | `http://localhost:8080` |
| `VITE_BACKEND_PORT` | Backend API port | `8080` |
| `VITE_KEYCLOAK_URL` | Keycloak URL for frontend authentication | `http://localhost:8081` |
| `VITE_UNLAYER_PUBLIC_KEY` | Unlayer email editor API key | `your_unlayer_api_key` |

### **Email Service (Mailjet)**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `MAILJET_API_USERNAME` | Mailjet API key/username for sending emails | `your_mailjet_api_key` |
| `MAILJET_API_PASSWORD` | Mailjet API secret/password | `your_mailjet_secret_key` |

### **Feature Flags (Flagsmith)**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `VITE_FLAGSMITH_ENV_ID` | Flagsmith environment ID for feature flag management | `your_flagsmith_env_id` |

### **API Versioning**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `VITE_API_VERSION` | API version identifier | `v1` or `v2` or `v3` |

### **Network**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `MACHINE_URL` | Public IP or domain name of the host machine | `localhost` or your public IP |

---

### **Container Ports**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `backend_docker_port` | Internal backend container port | `8080` |
| `frontend_docker_port` | Internal frontend container port | `5173` |

### **Spring Boot Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `spring_profiles_active` | Active Spring profile | `"prod"` |
| `spring_security_issuer_uri` | OAuth2 issuer URI for Terraform deployments | `http://your-host:8081/realms/your-realm` |
| `spring_security_jwk_uri` | JWK Set URI for token validation | `http://keycloak:8080/realms/your-realm/protocol/openid-connect/certs` |

### **Email Service Credentials**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `mailjet_api_username` | Mailjet API username | `your_mailjet_api_key` |
| `mailjet_api_password` | Mailjet API password | `your_mailjet_secret_key` |

### **Frontend Environment Variables**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `vite_backend_host` | Backend URL for frontend | `http://your-host:8080` |
| `vite_backend_port` | Backend port | `"8080"` |
| `vite_keycloak_url` | Keycloak URL for authentication | `http://your-host:8081` |
| `vite_flagsmith_env_id` | Flagsmith environment ID | `your_flagsmith_env_id` |
| `vite_api_version` | API version | `"v1"` or `"v2"` or `"v3"` |

### **Database Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `postgres_user` | PostgreSQL username | `"your_db_username"` |
| `postgres_password` | PostgreSQL password | `"your_secure_db_password"` |
| `postgres_db` | Database name | `"your_database_name"` |
| `postgres_local_port` | External port mapping | `5433` |
| `postgres_docker_port` | Internal container port | `5432` |

### **Container Scaling**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `backend_replicas` | Number of backend container instances | `1` or `2` or more |
| `frontend_replicas` | Number of frontend container instances | `1` or `2` or more |

### **Load Balancer Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `frontend_lb_external_port` | External port for frontend load balancer | `3000` |
| `frontend_lb_internal_port` | Internal port for frontend load balancer | `80` |
| `backend_lb_external_port` | External port for backend load balancer | `8080` |
| `backend_lb_internal_port` | Internal port for backend load balancer | `80` |

### **Observability (OpenTelemetry)**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `otel_exporter_otlp_endpoint` | Elasticsearch APM endpoint for telemetry data | `https://your-apm-endpoint.example.com` |
| `otel_exporter_otlp_headers` | Authentication headers for APM (base64 encoded) | `base64_encoded_credentials` |

### **AI Services (Google Gemini)**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `gemini_api_key` | Google Gemini API key for AI features | `your_gemini_api_key` |
| `google_project_id` | Google Cloud project ID | `your_gcp_project_id` |

### **Facebook Integration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `facebook_access_token` | Facebook Graph API access token | `your_facebook_access_token` |
| `facebook_app_id` | Facebook App ID | `your_facebook_app_id` |
| `facebook_app_secret` | Facebook App Secret key | `your_facebook_app_secret` |
| `facebook_page_id` | Facebook Page ID (internal) | `your_facebook_page_id` |
| `facebook_public_page_id` | Facebook Page ID (public-facing) | `your_public_page_id` |

### **HubSpot Integration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `hubspot_access_token` | HubSpot API access token for CRM integration | `your_hubspot_access_token` |

### **Network Configuration**
| Variable | Description | Example Value |
|----------|-------------|---------------|
| `machine_url` | Public IP address or domain name | `"localhost"` or `"your.domain.com"` or `"192.168.x.x"` |

---

## **Key Differences Between `.env` and `terraform.tfvars`**

| Aspect | `.env` | `terraform.tfvars` |
|--------|--------|-------------------|
| **Purpose** | Docker Compose local development | Terraform infrastructure provisioning |
| **Usage** | Development and testing | Production deployment |
| **Docker-specific** | Yes (container names, volumes) | No |
| **Scaling** | Not supported | Supports replicas and load balancing |
| **Format** | `KEY=value` | `key = "value"` (HCL syntax) |

---

## **Setup Instructions**

### **1. Copy Example Files**

Files can be created by copying the content below into `.env` and `terraform.tfvars` respectively.

```bash
echo <<EOF > .env
KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:24.0.2
KEYCLOAK_ADMIN=
KEYCLOAK_ADMIN_PASSWORD=
KEYCLOAK_LOCAL_PORT=
KEYCLOAK_DOCKER_PORT=
KEYCLOAK_REALM=
KEYCLOAK_IMPORT_PATH=./keycloak/realms
KC_HOSTNAME_URL=


SPRING_PROFILES_ACTIVE=prod
SPRING_SECURITY_ISSUER_URI=
SPRING_SECURITY_JWK_URI=


BACKEND_LOCAL_PORT=
BACKEND_DOCKER_PORT=
SPRING_PROFILES_ACTIVE=prod
SPRING_SECURITY_ISSUER_URI=
SPRING_SECURITY_JWK_URI=


POSTGRES_DB=
POSTGRES_USER=
POSTGRES_PASSWORD=
PG_LOCAL_PORT=
PG_DOCKER_PORT=


FRONTEND_LOCAL_PORT=    
FRONTEND_DOCKER_PORT=
VITE_BACKEND_HOST=
VITE_BACKEND_PORT=
VITE_KEYCLOAK_URL=
VITE_UNLAYER_PUBLIC_KEY=


MAILJET_API_USERNAME=
MAILJET_API_PASSWORD=

VITE_FLAGSMITH_ENV_ID=


VITE_API_VERSION=v1
VITE_BACKEND_HOST=

MACHINE_URL=localhost
```

```bash
echo <<EOF > terraform.tfvars
backend_docker_port = 

frontend_docker_port = 

spring_profiles_active  = "prod"
spring_security_issuer_uri = 
spring_security_jwk_uri = 

mailjet_api_username = 
mailjet_api_password = 

vite_backend_host = 
vite_backend_port = 

vite_keycloak_url = 

vite_flagsmith_env_id = 

vite_api_version = "v1"

postgres_user = "user"
postgres_password = "password"
postgres_db = 
postgres_local_port = 
postgres_docker_port = 

backend_replicas  = 1
frontend_replicas = 1

frontend_lb_external_port = 
frontend_lb_internal_port = 

backend_lb_external_port = 
backend_lb_internal_port = 

otel_exporter_otlp_endpoint = 
otel_exporter_otlp_headers = 

gemini_api_key = 
google_project_id = 

facebook_access_token = 
facebook_app_id = 
facebook_app_secret = 
facebook_page_id = 
facebook_public_page_id = 
hubspot_access_token = 

machine_url = "localhost"
```

### **2. Edit Configuration Files**
Update both `.env` and `terraform.tfvars` with your actual values:
- Replace API keys and tokens
- Update URLs if deploying to non-localhost
- Configure database credentials
- Set proper network configuration

### **3. Automatic IP Configuration**
Use the provided script to automatically configure your machine's IP:
```bash
cd proj
./setup-machine-ip.sh
```

This script updates:
- `terraform.tfvars`
- `.env`
- Keycloak redirects
- Frontend environment files

---

## **Security Notes**

⚠️ **Important Security Considerations:**

1. **Never commit** `.env` or `terraform.tfvars` files to version control
2. **Use example files** (`.env.example`, `terraform.tfvars.example`) with placeholder values
3. **Rotate secrets regularly** (API keys, passwords, tokens)
4. **Use strong passwords** for production databases
5. **Restrict access** to files containing sensitive information
6. **Use environment-specific values** (different credentials for dev/prod)

---

## **Troubleshooting**

### **Common Issues**

**Problem:** Services can't connect to each other
- **Solution:** Check that all URLs and ports match between `.env` and `terraform.tfvars`

**Problem:** Keycloak authentication fails
- **Solution:** Verify `SPRING_SECURITY_ISSUER_URI` and `VITE_KEYCLOAK_URL` are correctly set

**Problem:** Frontend can't reach backend
- **Solution:** Ensure `VITE_BACKEND_HOST` matches the backend URL and ports are correct

**Problem:** Database connection errors
- **Solution:** Verify PostgreSQL credentials and ports in both configuration files

**Problem:** External API integrations fail
- **Solution:** Check that all API keys and tokens are valid and not expired
