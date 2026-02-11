# DataShare

**DataShare** is a full-stack application with a **Spring Boot 4** backend API and an **Angular 21** frontend web application, designed with strong testing, security, and automation practices.

## Table of Contents

1. [Monorepo Structure](#monorepo-structure)
2. [Requirements](#requirements)
3. [Quick start](#quick-start)
4. [Main features](#main-features)
5. [Quality](docs/QUALITY.md)
6. [Testing](docs/TESTING.md)
7. [Maintenance](docs/MAINTENANCE.md)
8. [Performance](docs/PERFORMANCE.md)
9. [Security](docs/SECURITY.md)
10. [Quick reference guide (Makefile commands)](docs/QUICK-REFERENCE.md)

---

## Monorepo Structure

```
datashare/
├── datashare-api/............................ Spring Boot API
├── datashare-web/............................ Angular Web app
├── scripts/.................................. scripts folder
├── compose.yaml.............................. Local environment (PostgreSQL, localstack)
├── VERSION................................... Current application version
├── .env...................................... Development Environment variables file
├── openapi.yaml.............................. OpenAPI specification
├── Makefile.................................. Makefile for build tasks automation
```

---

## Requirements

- **Node.js** >= 20.x
- **Java 21** 
- **Maven** >= 4.x (wrapper included in `datashare-api`)
- **Docker & Docker Compose**
- **AWS command line tool** 
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

## Main features
- Upload file using presigned S3 URL
- File metadata storage in PostgreSQL database
- User upload list
- Secure download from random token
- Authentication via cookie HttpOnly

---

### | [Table of Contents](#table-of-contents) |[Testing](docs/TESTING.md) | [Maintenance](docs/MAINTENANCE.md) | [Performance](docs/PERFORMANCE.md) | [Security](docs/SECURITY.md) | [Quick reference](docs/QUICK-REFERENCE.md) |
