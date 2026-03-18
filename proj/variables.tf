variable "backend_docker_port" {
  description = "Internal port for backend container"
  type        = number
}

variable "frontend_docker_port" {
  description = "Internal port for frontend container"
  type        = number
}


variable "spring_profiles_active" {
  description = "Spring profile to activate"
  type        = string
}

variable "spring_security_issuer_uri" {
  description = "OAuth2 issuer URI"
  type        = string
}

variable "spring_security_jwk_uri" {
  description = "OAuth2 JWK set URI"
  type        = string
}

variable "mailjet_api_username" {
  description = "Mailjet API username"
  type        = string
  sensitive   = true
}

variable "mailjet_api_password" {
  description = "Mailjet API password"
  type        = string
  sensitive   = true
}

variable "vite_backend_host" {
  description = "Backend host for frontend"
  type        = string
  default     = "localhost"
}

variable "vite_backend_port" {
  description = "Backend port for frontend"
  type        = string
}

variable "vite_keycloak_url" {
  description = "Keycloak URL for frontend"
  type        = string
}


variable "vite_flagsmith_env_id" {
  description = "Flagsmith environment ID"
  type        = string
}

variable "vite_api_version" {
  description = "API version"
  type        = string
  default     = "v1"
}

variable "postgres_user" {
  description = "PostgreSQL database user"
  type        = string
}

variable "postgres_password" {
  description = "PostgreSQL database password"
  type        = string
  sensitive   = true
}

variable "postgres_db" {
  description = "PostgreSQL database name"
  type        = string
  sensitive   = true
}

variable "postgres_local_port" {
  description = "External port for PostgreSQL container"
  type        = number
}

variable "postgres_docker_port" {
  description = "Internal port for PostgreSQL container"
  type        = number
}

variable "frontend_replicas" {
  description = "Number of frontend container replicas"
  type        = number
  default     = 1
}

variable "backend_replicas" {
  description = "Number of backend container replicas"
  type        = number
  default     = 1
}

variable "frontend_lb_internal_port" {
  description = "Internal port for frontend load balancer"
  type        = number
}

variable "frontend_lb_external_port" {
  description = "External port for frontend load balancer"
  type        = number
}

variable "backend_lb_internal_port" {
  description = "Internal port for backend load balancer"
  type        = number
}

variable "backend_lb_external_port" {
  description = "External port for backend load balancer"
  type        = number
}

variable "otel_exporter_otlp_endpoint" {
  description = "OTEL Exporter OTLP Endpoint"
  type        = string
}

variable "otel_exporter_otlp_headers" {
  description = "OTEL Exporter OTLP Headers"
  type        = string
}

variable "gemini_api_key" {
  description = "Chave da API Gemini para serviços de IA"
  type        = string
  sensitive   = true # Recomendado para chaves secretas
}


variable "google_project_id" {
  description = "ID do projeto Google Cloud (para serviços Gemini/Google AI)"
  type        = string
}

# FACEBOOK
variable "facebook_access_token" {
  description = "Facebook Access Token"
  type        = string
  sensitive   = true
}

variable "facebook_app_id" {
  description = "Facebook App ID"
  type        = string
}

variable "facebook_app_secret" {
  description = "Facebook App Secret"
  type        = string
  sensitive   = true
}

variable "facebook_page_id" {
  description = "Facebook Page ID"
  type        = string
}

variable "facebook_public_page_id" {
  type = string
}

variable "hubspot_access_token" {
  description = "HubSpot Access Token"
  type        = string
}

variable "machine_url" {
  description = "Machine URL"
  type        = string
}