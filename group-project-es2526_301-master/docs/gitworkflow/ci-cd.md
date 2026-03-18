# CI/CD Pipeline Documentation

## Overview

This project uses GitHub Actions for Continuous Integration and Continuous Deployment (CI/CD). The pipeline is designed to automatically build, test, and deploy the application when changes are pushed to the repository.

## Workflows

### 1. CI - App Run + Tests (`ci_docker_compose_and_tests.yaml`)

**Trigger:** Pull requests to any branch

**Purpose:** Validates code changes by building the application, running all automated tests, and verifying the system works end-to-end.

**Runner:** Self-hosted (`es2526-301-local`)

### 2. CD - Deploy to Production (`cd_deploy.yaml`)

**Trigger:** Merges to the `main` branch
**Purpose:** Deploys the latest version of the application to the production environment.
**Runner:** Self-hosted (`deti-engsoft-11`)


## Test Strategy

### Test Types

1. **End-to-End Tests (Selenium + Cucumber)**
   - User authentication flows
   - Campaign creation and management
   - Email template creation
   - Segment management
   - Marketing automation workflows

2. **Integration Tests**
   - Backend API endpoints
   - Database operations
   - Keycloak authentication integration

### Test Data

Test data is initialized via `proj/backend/src/main/resources/data.sql`:
- Pre-configured users with different roles
- Sample campaigns
- Sample segments
- Email templates

