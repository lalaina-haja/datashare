# DataShare

**DataShare** is a full-stack application with a **Spring Boot 4 API (`api`)** and an **Angular frontend (`web`)**, designed with strong testing, security, and automation practices.

## Table of Contents

1. [Monorepo Structure](#monorepo-structure)
2. [Requirements](#requirements)
3. [Environment Setup](#environment-setup)
4. [Running the Applications](#running-the-applications)
5. [Quality](docs/QUALITY.md)
6. [Testing](docs/TESTING.md)
7. [Maintenance](docs/MAINTENANCE.md)
8. [Performance](docs/PERFORMANCE.md)
9. [Security](docs/SECURITY.md)
10. [Quick reference (Makefile commands)](docs/QUICK-REFERENCE.md)

---

## Monorepo Structure

```
datashare/
├── datashare-api/ # Spring Boot API
├── datashare-web/ # Angular Web app
│ └── tests/config/ # Jest / Cypress / Playwright configs
├── compose.yaml # API dependencies (PostgreSQL, S3, etc.)
├── scripts/bump-version.sh # Script to bump SemVer version
│ └── git-hooks # git hooks scripts
├── VERSION # Current application version
├── .env # Environment variables
├── openapi.yaml # OpenAPI specification
├── Makefile # Commands for starting apps, building, testing, linting, versioning
```

---

## Requirements

- **Node.js** >= 20.x
- **npm** >= 10.x
- **Java 21** (Spring Boot 4)
- **Maven** >= 4.x (wrapper included in `datashare-api`)
- **Docker & Docker Compose**
- **VS Code** (recommended IDE)

---

## Quick Start

1. Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

2. Update environment variables in `.env` file

3. Install web dependencies:

```bash
make install-web
```

4. Optional: Install git hooks (for development use):

```bash
make install-hooks
```

5. Running the Applications

- Start the backend (API)
```bash
make start-api
```

- Start the frontend (Web app)
```bash
make start-web
```

- Start everything together
```bash
make start-all
```

---

### | [Table of Contents](#table-of-contents) |[Testing](docs/TESTING.md) | [Maintenance](docs/MAINTENANCE.md) | [Performance](docs/PERFORMANCE.md) | [Security](docs/SECURITY.md) | [Quick reference](docs/QUICK-REFERENCE.md) |
