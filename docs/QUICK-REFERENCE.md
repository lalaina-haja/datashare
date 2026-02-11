# Quick Reference – Makefile Commands

| Category | Target | Description |
|----------|--------|-------------|
| **Applications** | `make start-api` |  Start datashare-api (Spring Boot Application) |
|  | `make start-web` | Start datashare-web (Angular dev server) |
|  | `make start-all` | Start API + Web |
| **Build** | `make build-api` | Build datashare-api |
|  | `make build-web` | Build datashare-web |
|  | `make build-all` | Build both API + Web |
| **Web Dependencies** | `make install-web` | Install Web dependencies |
| **Linting** | `make lint-api` | API lint (Compile / Spotless / Checkstyle) |
|  | `make lint-web` | Web lint (Angular Lint) |
|  | `make lint` | Lint both API + Web |
| **API Tests** | `make test-api-unit` | API unit tests |
|  | `make test-api-integ` | API integration tests |
| **Web Tests** | `make test-web-unit` | Web unit tests (Vitest) |
|  | `make test-web-integ` | Web integration tests (Vitest) |
| **End to End Tests** | `make test-e2e` | End-to-end tests (Cypress + Cucumber) |
| **Combined Tests** | `make test-unit` | All unit tests |
|  | `make test-integ` | All integration tests |
|  | `make test-all` | All tests |
| **Versioning** | `make version` | Show the applciation current version |
|  | `make bump-version TYPE=patch/minor/major` | Bump version  |


➡️ Full list: `make help`
---
### | [⬅ Back to DataShare README](../README.md) |