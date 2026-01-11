# Quick Reference – Makefile Commands

| Category | Target | Description |
|----------|--------|-------------|
| **Services** | `make start-services` | Start API dependencies (Postgres, S3, etc.) |
|  | `make stop-services` | Stop API services |
|  | `make start-api` | Start API (depends on services) |
|  | `make start-web` | Start Web (auto-installs dependencies) |
|  | `make start-all` | Start API + Web + services |
| **Build** | `make build-api` | Build API |
|  | `make build-web` | Build Web production |
|  | `make build-all` | Build both API + Web |
| **Web Dependencies** | `make install-web` | Install Web dependencies |
| **Linting** | `make lint-api` | API lint (Checkstyle / SpotBugs / PMD) |
|  | `make lint-web` | Web lint (ESLint / Angular) |
|  | `make lint` | Lint both API + Web |
| **API Tests** | `make test-api-unit` | API unit tests |
|  | `make test-api-it` | API integration tests |
|  | `make test-api-e2e` | API end-to-end tests |
| **Web Tests** | `make test-web-unit` | Web unit tests (Jest) |
|  | `make test-web-it` | Web integration tests |
|  | `make test-web-e2e` | Web end-to-end tests (Cypress + Playwright) |
| **Combined Tests** | `make test-unit` | All unit tests |
|  | `make test-it` | All integration tests |
|  | `make test-e2e` | All end-to-end tests |
|  | `make test-all` | All tests |
| **Versioning** | `make version` | Show version (`VERSION`) |
|  | `make bump-version TYPE=patch|minor|major` | Bump version via `scripts/bump-version.sh` |
make sync-version

➡️ Full list: make help
---
### | [⬅ Back to DataShare README](../README.md) |