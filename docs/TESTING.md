# Testing

## API Tests (Maven profiles)

| Target | Description |
|--------|-------------|
| `make test-api-unit` | API unit tests |
| `make test-api-it` | API integration tests (requires services) |
| `make test-api-e2e` | API end-to-end tests (requires services) |

## Web Tests (Jest / Cypress / Playwright)

| Target | Description |
|--------|-------------|
| `make test-web-unit` | Jest unit tests |
| `make test-web-it` | Jest integration tests |
| `make test-web-e2e` | Cypress + Playwright end-to-end tests |

**Note:** Test configuration files are in `datashare-web/tests/config/`.

## Combined Test Targets

| Target | Description |
|--------|-------------|
| `make test-unit` | Run all unit tests (API + Web) |
| `make test-it` | Run all integration tests (API + Web) |
| `make test-e2e` | Run all E2E tests (API + Web) |
| `make test-all` | Run all tests sequentially |

⚠️ **TODO:** This page is under construction

---
### | [⬅ Back to DataShare README](../README.md) |