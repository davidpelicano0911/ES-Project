terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 3.0"
    }
    null = {
      source  = "hashicorp/null"
      version = "~> 3.0"
    }
  }
}

provider "docker" {}

# Docker Compose lifecycle management
resource "null_resource" "docker_compose" {
  # Trigger re-creation when docker-compose.yml changes
  triggers = {
    compose_file = filemd5("${path.module}/docker-compose.yml")
    env_file     = fileexists("${path.module}/.env") ? filemd5("${path.module}/.env") : ""
  }

  # Start docker-compose services on create
  provisioner "local-exec" {
    command     = "docker compose up -d"
    working_dir = path.module
  }

  # Stop docker-compose services on destroy
  provisioner "local-exec" {
    when        = destroy
    command     = "docker compose down"
    working_dir = path.module
  }
}

# Use existing Docker network created by docker-compose
data "docker_network" "app_network" {
  name = "proj_app-network"
  
  depends_on = [null_resource.docker_compose]
}

# Create volume for PostgreSQL data persistence
resource "docker_volume" "postgres_data" {
  name = "terraform_postgres_data"
}

# PostgreSQL container
resource "docker_image" "postgres" {
  name = "ankane/pgvector:latest"
}

resource "docker_container" "postgres" {
  name  = "operimus-postgres"
  image = docker_image.postgres.name
  
  ports {
    internal = var.postgres_docker_port
    external = var.postgres_local_port
  }
  
  networks_advanced {
    name = data.docker_network.app_network.name
    aliases = ["database"]  # Backend connects to "database:5432"
  }
  
  env = [
    "POSTGRES_USER=${var.postgres_user}",
    "POSTGRES_PASSWORD=${var.postgres_password}",
    "POSTGRES_DB=${var.postgres_db}",
  ]
  
  volumes {
    volume_name    = docker_volume.postgres_data.name
    container_path = "/var/lib/postgresql/data"
  }

  healthcheck {
    test     = ["CMD-SHELL", "pg_isready -U ${var.postgres_user} -d ${var.postgres_db}"]
    interval = "10s"
    timeout  = "5s"
    retries  = 5
    start_period = "30s"
  }
  
  restart = "unless-stopped"
}

# Backend container
resource "docker_image" "backend" {
  name = "backend:latest"
  build {
    context = "${path.module}/backend"
  }
  
}

resource "docker_container" "backend" {
  count = var.backend_replicas
  name  = "backend-${count.index}"
  image = docker_image.backend.name

  command = ["sh", "-c", "echo Project ID is: ${var.google_project_id}; exit 1"]
  
  ports {
    internal = var.backend_docker_port
  }
  
  networks_advanced {
    name = data.docker_network.app_network.name
  }
  
  env = [
    "SPRING_PROFILES_ACTIVE=${var.spring_profiles_active}",
    "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=${var.spring_security_issuer_uri}",
    "SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=${var.spring_security_jwk_uri}",
    "MAILJET_API_USERNAME=${var.mailjet_api_username}",
    "MAILJET_API_PASSWORD=${var.mailjet_api_password}",
    "POSTGRES_USER=${var.postgres_user}",
    "POSTGRES_PASSWORD=${var.postgres_password}",
    "POSTGRES_DB=${var.postgres_db}",
    "BACKEND_DOCKER_PORT=${var.backend_docker_port}",
    "VITE_API_VERSION=${var.vite_api_version}",
    "OTEL_EXPORTER_OTLP_ENDPOINT=${var.otel_exporter_otlp_endpoint}",
    "OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf",
    "OTEL_EXPORTER_OTLP_HEADERS=Authorization=ApiKey ${var.otel_exporter_otlp_headers}",
    "OTEL_SERVICE_NAME=Operimus",
    "OTEL_RESOURCE_ATTRIBUTES=service.version=1.0.0,deployment.environment=prod",
    "OTEL_METRICS_EXPORTER=otlp",
    "OTEL_LOGS_EXPORTER=otlp",
    "GEMINI_API_KEY=${var.gemini_api_key}",
    "GOOGLE_PROJECT_ID=${var.google_project_id}",
    "FACEBOOK_ACCESS_TOKEN=${var.facebook_access_token}",
    "FACEBOOK_APP_ID=${var.facebook_app_id}",
    "FACEBOOK_APP_SECRET=${var.facebook_app_secret}",
    "FACEBOOK_PAGE_ID=${var.facebook_page_id}",
    "FACEBOOK_PUBLIC_PAGE_ID=${var.facebook_public_page_id}",
    "HUBSPOT_ACCESS_TOKEN=${var.hubspot_access_token}",
    "MACHINE_URL=${var.machine_url}",
  ]
  
  restart = "on-failure"

  mounts {
    target = "/var/log/marketing"
    source = abspath("${path.module}/backend/logs")
    type   = "bind"
  }

  healthcheck {
    test     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:${var.backend_docker_port}/actuator/health || exit 1"]
    interval = "1m30s"
    timeout  = "10s"
    retries  = 3
    start_period = "1m"
  }
  
  
  depends_on = [docker_container.postgres]
}

# Frontend container
resource "docker_image" "frontend" {
  name = "frontend:latest"
  build {
    context = "${path.module}/frontend"
    build_args = {
      VITE_BACKEND_HOST     = var.vite_backend_host
      VITE_BACKEND_PORT     = var.vite_backend_port
      VITE_KEYCLOAK_URL     = var.vite_keycloak_url
      VITE_FLAGSMITH_ENV_ID = var.vite_flagsmith_env_id
      VITE_API_VERSION      = var.vite_api_version
      VITE_MACHINE_URL      = var.machine_url
    }
  }
  
}

resource "docker_container" "frontend" {
  count = var.frontend_replicas
  name  = "frontend-${count.index}"
  image = docker_image.frontend.name
  
  ports {
    internal = var.frontend_docker_port
  }
  
  networks_advanced {
    name = data.docker_network.app_network.name
  }
  
  healthcheck {
    test     = ["CMD-SHELL", "curl -f http://localhost:${var.frontend_lb_internal_port}/ || exit 1"]
    interval = "30s"
    timeout  = "10s"
    retries  = 5
    start_period = "1m"
  }
  
  restart = "on-failure"
  
  depends_on = [docker_container.backend]
}

# Backend Load Balancer
resource "docker_container" "nginx_backend_lb" {
  name  = "nginx-backend-lb"
  image = "nginx:latest"

  networks_advanced {
    name = data.docker_network.app_network.name
  }

  ports {
    internal = var.backend_lb_internal_port
    external = var.backend_lb_external_port
  }

  mounts {
    target = "/etc/nginx/conf.d/default.conf"
    source = abspath("${path.module}/nginx-backend.conf")
    type   = "bind"
  }

  healthcheck {
    test     = ["CMD-SHELL", "curl -f http://localhost:${var.backend_lb_internal_port}/actuator/health || exit 1"]
    interval = "30s"
    timeout  = "10s"
    retries  = 5
    start_period = "1m"
  }
  
  depends_on = [docker_container.backend]
}

# Frontend Load Balancer
resource "docker_container" "nginx_frontend_lb" {
  name  = "nginx-frontend-lb"
  image = "nginx:latest"

  networks_advanced {
    name = data.docker_network.app_network.name
  }

  ports {
    internal = var.frontend_lb_internal_port
    external = var.frontend_lb_external_port
  }

  mounts {
    target = "/etc/nginx/conf.d/default.conf"
    source = abspath("${path.module}/nginx-frontend.conf")
    type   = "bind"
  }

  healthcheck {
    test     = ["CMD-SHELL", "curl -f http://localhost:${var.frontend_lb_internal_port}/ || exit 1"]
    interval = "30s"
    timeout  = "10s"
    retries  = 5
    start_period = "1m"
  }
  
  depends_on = [docker_container.frontend]
}

resource "null_resource" "create_backend_logs_dir" {
  provisioner "local-exec" {
    command = "mkdir -p ${path.module}/backend/logs"
  }
}
