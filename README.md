# DataShare

**DataShare** is a full-stack application with a **Spring Boot 4 API (`api`)** and an **Angular frontend (`web`)**, designed with strong testing, security, and automation practices.

## Table of Contents

1. [Monorepo Structure](#monorepo-structure)  
2. [Requirements](#requirements)  
3. [Environment Setup](#environment-setup) 
5. [VS Code Configuration](#vs-code-configuration)   
6. [Running the Applications](#running-the-applications)  
7. [Testing](docs/TESTING.md)
8. [Maintenance](docs/MAINTENANCE.md)
9. [Performance](docs/PERFORMANCE.md)
10. [Security](docs/SECURITY.md)
11. [Quick reference (Makefile commands)](docs/QUICK-REFERENCE.md)
12. [Notes](#notes)

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

## Environment Setup

1. Copy `.env.example` to `.env`:

```bash
cp .env.example .env
```

2. Update environment variables:
```properties
# Application ports (make sure they are available)
API_PORT=8080
WEB_PORT=4200

# Security (change JWT_SECRET value to a real secret)
JWT_SECRET=your-jwt-secret-minimum-256
JWT_EXPIRATION=3600000

# Database 
DB_NAME=database_name
DB_USER=database_user
DB_PASSWORD=datashare_password

# AWS S3
AWS_REGION=aws_localstack_region
AWS_S3_BUCKET=bucket_name
```

3. Install web dependencies:
```bash
make install-web
```

---

## VS Code Configuration
- Workspace prevents duplicate compilation errors
- Linters run only in corresponding apps (api or web)
- Recommended extensions:
    - Java Extension Pack (Spring Boot)
    - Angular Language Service
    - ESLint
    - Prettier

---

## Running the Applications

### Start the backend (API)
```bash
make start-api
```

### Start the frontend (Web app)
```bash
make start-web
```
Automatically installs dependencies if needed.

### Start everything together
```bash
make start-all
```

---
### | [Table of Contents](#table-of-contents) |[Testing](docs/TESTING.md) | [Maintenance](docs/MAINTENANCE.md) | [Performance](docs/PERFORMANCE.md) | [Security](docs/SECURITY.md) | [Quick reference](docs/QUICK-REFERENCE.md) |

