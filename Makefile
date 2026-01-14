# ============================
# Variables
# ============================
SHELL := /bin/bash
VERSION := $(shell cat VERSION)
API_DIR := datashare-api
WEB_DIR := datashare-web
DOCKER_COMPOSE_FILE=docker-compose.yml

# ============================
# Help
# ============================
.PHONY: help
help:
	@echo "DataShare Monorepo Makefile"
	@echo ""
	@echo "Services:"
	@echo "  make start-services       Start API dependencies (Postgres, S3, etc.)"
	@echo "  make stop-services        Stop API services"
	@echo "  make start-api            Start datashare-api (requires services)"
	@echo "  make start-web            Start datashare-web (Angular dev server)"
	@echo "  make start-all            Start API + Web + services"
	@echo ""
	@echo "Build:"
	@echo "  make build-api            Build the API (Spring Boot package)"
	@echo "  make build-web            Build the Web (Angular production build)"
	@echo "  make build-all            Build both API and Web"
	@echo ""
	@echo "Web dependencies:"
	@echo "  make install-web          Install all Web dependencies"
	@echo ""
	@echo "Linting:"
	@echo "  make lint-api             Run API lint (Checkstyle / SpotBugs / PMD)"
	@echo "  make lint-web             Run Web lint (ESLint / Angular)"
	@echo "  make lint                 Run all lint checks (API + Web)"
	@echo ""
	@echo "API Tests (Maven profiles):"
	@echo "  make test-api-unit        Run API unit tests"
	@echo "  make test-api-it          Run API integration tests (requires services)"
	@echo "  make test-api-e2e         Run API end-to-end tests (requires services)"
	@echo ""
	@echo "Web Tests (Jest / Cypress / Playwright):"
	@echo "  make test-web-unit        Run Web unit tests"
	@echo "  make test-web-it          Run Web integration tests"
	@echo "  make test-web-e2e         Run Web end-to-end tests"
	@echo ""
	@echo "Combined Test Targets:"
	@echo "  make test-unit            Run all unit tests (API + Web)"
	@echo "  make test-it              Run all integration tests (API + Web)"
	@echo "  make test-e2e             Run all end-to-end tests (API + Web)"
	@echo "  make test-all             Run all tests sequentially"
	@echo ""
	@echo "Versioning:"
	@echo "  make version            Show current version (VERSION file)"
	@echo "  make sync-version       Sync VERSION -> datashare-web/package.json"
	@echo "  make bump-version       Bump version (TYPE=patch|minor|major)"
	@echo ""
	@echo "Git hooks:"
	@echo "  make install-hooks        Install git hooks"
	@echo "  make uninstall-hooks      Uninstall git hooks"
	@echo ""

# ============================
# Versioning
# ============================
VERSION_FILE=VERSION
BUMP_SCRIPT=./scripts/bump-version.sh

.PHONY: version
version:
	@cat $(VERSION_FILE)

.PHONY: bump-version
bump-version:
ifndef TYPE
	$(error "You must provide TYPE=patch|minor|major, e.g., make bump-version TYPE=minor")
endif
	@echo "Bumping version ($(TYPE))..."
	$(BUMP_SCRIPT) $(TYPE)
	@echo "New version: $$(cat $(VERSION_FILE))"

.PHONY: sync-version
sync-version:
	@./scripts/sync-version-web.sh

# ============================
# Backend services (Docker)
# ============================
.PHONY: start-services
start-services:
	@echo "Starting backend services..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) up -d

.PHONY: stop-services
stop-services:
	@echo "Stopping backend services..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) down

# ============================
# Web dependencies
# ============================
.PHONY: install-web
install-web: sync-version
	@echo "Installing datashare-web dependencies..."
	cd datashare-web && npm install

# ============================
# Start backend / frontend
# ============================
.PHONY: start-api
start-api: start-services
	@echo "Starting backend..."
	cd $(API_DIR) && ./mvnw spring-boot:run

.PHONY: start-web
start-web: install-web
	@echo "Starting frontend..."
	cd $(WEB_DIR) && npm install && npm run start

.PHONY: start-all
start-all: start-services
	@echo "Starting backend + frontend..."
	$(MAKE) start-api &
	$(MAKE) start-web &

# ============================
# Linting (Angular / TypeScript / Checkstyle / SpotBugs / PMD)
# ============================
.PHONY: lint-web lint-api lint
lint-web: install-web
	@echo "Running datashare-web lint (Angular / TypeScript)..."	
	cd $(WEB_DIR) && npm run lint

lint-api:
	@echo "Running datashare-api lint (Checkstyle / SpotBugs / PMD)..."
	cd $(API_DIR) && ./mvnw verify -DskipTests

lint: lint-api lint-web
	@echo "All lint checks completed."

# ============================
# Building (Angular / Maven)
# ============================
.PHONY: build-web build-api build-all
build-web: install-web
	@echo "Building datashare-web (Angular)..."
	cd $(WEB_DIR) && npm run build

build-api: start-services
	@echo "Building datashare-api..."
	cd $(API_DIR) && ./mvnw clean package -DskipTests

build-all: build-api build-web
	@echo "All applications built successfully."

# ============================
# Frontend tests (npm / Jest / Cypress / Playwright)
# ============================
.PHONY: test-web-unit test-web-it test-web-e2e
test-web-unit: install-web
	@echo "Running frontend unit tests..."
	cd $(WEB_DIR) && npm run test-unit 

test-web-it: install-web
	@echo "Running frontend integration tests..."
	cd $(WEB_DIR) && npm run test-it

test-web-e2e: install-web
	@echo "Running frontend end-to-end tests..."
	cd $(WEB_DIR) && npm run test-e2e  

# ============================
# Backend tests (Maven)
# ============================
.PHONY: test-api-unit test-api-it test-api-e2e
test-api-unit:
	@echo "Running backend unit tests..."
	cd $(API_DIR) && ./mvnw test -Punit

test-api-it:
	@echo "Running backend integration tests..."
	cd $(API_DIR) && ./mvnw verify -Pit

test-api-e2e: start-services
	@echo "Running backend end-to-end tests..."
	cd $(API_DIR) && ./mvnw verify -Pe2e


# ============================
# Combined targets
# ============================
.PHONY: test-unit
test-unit: test-api-unit test-web-unit
	@echo "All unit tests finished."

.PHONY: test-it
test-it: test-api-it test-web-it
	@echo "All integration tests finished."

.PHONY: test-e2e
test-e2e: test-api-e2e test-web-e2e
	@echo "All E2E tests finished."

.PHONY: test-all
test-all: test-unit test-it test-e2e
	@echo "All tests finished."

# ============================
# Git hooks
# ============================
GIT_HOOKS_DIR=scripts/git-hooks
.PHONY: install-hooks uninstall-hooks

install-hooks:
	@echo "Installing git hooks..."
	@git config core.hooksPath $(GIT_HOOKS_DIR)	
	@echo "Git hooks installed."

uninstall-hooks:
	@echo "Uninstalling git hooks..."
	@git config --unset core.hooksPath
	@echo "Git hooks uninstalled."
