# PWI-Aufgabendatenbank — Setup Guide

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| Java | 21 (Temurin) | Required for backend |
| Node.js | 22 | Required for frontend |
| Maven | 3.9+ | Wrapper included (`mvnw`/`mvnw.cmd`) |
| Docker | 24+ | For local dev and production deployment |
| Docker Compose | v2 | Included with Docker Desktop |

---

## Local Development

### 1. Full stack with Docker Compose (easiest)

```bash
docker compose up -d
```

This starts:
- **PostgreSQL 16** on port `5432`
- **Backend** (Spring Boot) on port `8080`
- **Frontend** (Vite dev server) on port `8085` with hot-reload

The database is seeded automatically from `database/init/init.sql`.

### 2. Backend standalone

```bash
cd backend
./mvnw.cmd spring-boot:run
```

Requires a running PostgreSQL instance. Set env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

Or create a `.env` file in the `backend/` directory (loaded via `spring-dotenv`):

```env
DB_URL=jdbc:postgresql://localhost:5432/pwi_aufgabendatenbank
DB_USER=postgres
DB_PASSWORD=postgres
```

### 3. Frontend standalone (dummy/mock mode)

```bash
cd frontend
npm run dev:dummy
```

Runs on `http://localhost:8085` using local mock data — no backend needed.

### 4. Frontend standalone (real backend)

```bash
cd frontend
npm run dev
```

Proxies `/api` requests to `http://localhost:8080` (configurable via `VITE_PROXY_TARGET`).

---

## Running Tests

### Backend

```bash
cd backend
./mvnw.cmd test
```

### Frontend

```bash
cd frontend
npm test
```

---

## CI/CD Pipeline

Two GitHub Actions workflows are configured:

### `pr.yml` — Pull request checks

Runs automatically on every PR to `main`:

| Job | Steps |
|---|---|
| Backend | checkstyle → test → package |
| Frontend | eslint → type-check → test → build |

No Docker images are built; no deployment occurs.

### `main.yml` — Push to main (CI + deploy)

Runs when code is merged to `main`:

| Job | Steps |
|---|---|
| Backend | checkstyle → test → package → Docker build & push to GHCR |
| Frontend | eslint → type-check → test → build → Docker build & push to GHCR |
| Deploy | SSH into EC2 → pull images → `docker compose up -d` |

---

## Production Deployment (EC2)

### One-time EC2 setup

```bash
# Install Docker
sudo apt update && sudo apt install -y docker.io
sudo systemctl enable --now docker
sudo apt install -y docker-compose-plugin

# Create project directory
sudo mkdir -p /opt/pwi-datenbank
sudo chown $USER:$USER /opt/pwi-datenbank
```

### Required GitHub Secrets

The CI deploy job expects these secrets configured in the GitHub repository:

| Secret | Description | Example |
|---|---|---|
| `EC2_HOST` | EC2 public IP or DNS | `ec2-xx-xx-xx-xx.eu-central-1.compute.amazonaws.com` |
| `EC2_USER` | SSH username | `ubuntu` |
| `EC2_SSH_KEY` | Private SSH key (PEM) | `-----BEGIN RSA PRIVATE KEY-----...` |
| `DB_USER` | PostgreSQL user | `pwi_app` |
| `DB_PASSWORD` | PostgreSQL password | (random 32-char string) |
| `GHCR_PAT` | GitHub PAT with `write:packages` scope | `ghp_...` |

### How deployment works

1. On push to `main`, GitHub Actions builds both images and pushes them to `ghcr.io`.
2. The deploy job copies `docker-compose.prod.yml` to the EC2 instance via SCP.
3. It SSHs into the instance and runs:
   - Creates a `.env` file from secrets (if missing)
   - Pulls the latest `backend:latest` and `frontend:latest` images
   - Runs `docker compose -f docker-compose.prod.yml up -d`
   - Cleans up old images

The production `docker-compose.prod.yml`:
- Uses pre-built images from GHCR (no local builds)
- Includes a PostgreSQL container with persistent volume
- Sets `restart: always` on all services
- Frontend is served via nginx on port `80`
- Backend is not directly exposed — accessed through nginx proxy
- Services start in order: db → backend → frontend

---

## Code Quality

### Backend (Checkstyle)

Checkstyle enforces Google-style Java conventions with these settings:

- Max line length: **120**
- Indentation: **4 spaces**
- No Javadoc required on methods
- Test files have relaxed rules

Run manually:

```bash
cd backend
./mvnw.cmd checkstyle:check
```

### Frontend (ESLint)

ESLint 10 with flat config (`eslint.config.mjs`) enforces Vue 3 + TypeScript rules.

Run manually:

```bash
cd frontend
npm run lint
```

---

## Dependabot

Dependabot is configured in `.github/dependabot.yml` to create weekly PRs for:
- Maven dependencies (`backend/`)
- npm dependencies (`frontend/`)
- GitHub Actions updates

---

## Project Structure

```
.
├── .github/workflows/         # CI/CD pipelines
├── backend/                   # Spring Boot service (Java 21)
│   ├── checkstyle.xml         # Checkstyle rules
│   ├── checkstyle-suppressions.xml
│   ├── Dockerfile             # Local dev Docker build
│   ├── pom.xml
│   └── src/
├── frontend/                  # Vue 3 + Vite client
│   ├── Dockerfile             # Local dev Docker build
│   ├── Dockerfile.prod        # Production multi-stage build (nginx)
│   ├── nginx.conf             # Nginx config for production
│   ├── eslint.config.mjs
│   └── src/
├── database/
│   ├── init/init.sql          # Full schema + seed data
│   └── migrations/            # Flyway-style migrations
├── docs/                      # Documentation
├── docker-compose.yml         # Local dev compose
├── docker-compose.prod.yml    # Production compose (EC2)
└── .editorconfig              # Cross-IDE editor settings
```
