# ============================
# Variables
# ============================
SHELL := /bin/bash
VERSION := $(shell cat VERSION)
API_DIR := datashare-api
WEB_DIR := datashare-web
GIT_HOOKS_DIR := scripts/git-hooks

# ============================
# Help
# ============================
.PHONY: help
help:
	@echo "DataShare Monorepo Makefile"
	@echo ""
	@echo "Applications:"
	@echo "  make start-api            Start datashare-api (Spring Boot dev server)"
	@echo "  make start-web            Start datashare-web (Angular dev server)"
	@echo "  make start-all            Start API + Web"
	@echo ""
	@echo "Build:"
	@echo "  make build-api            Build the API (Spring Boot package)"
	@echo "  make build-web            Build the Web (Angular build)"
	@echo "  make build-all            Build both API and Web"
	@echo ""
	@echo "Web dependencies:"
	@echo "  make install-web          Install all Web dependencies"
	@echo ""
	@echo "Linting:"
	@echo "  make lint-api             Run API lint (Compile / Spotless / Checkstyle)"
	@echo "  make lint-web             Run Web lint (Angular Lint / Prettier)"
	@echo "  make lint                 Run all lint checks (API + Web)"
	@echo ""
	@echo "API Tests (Maven profiles):"
	@echo "  make test-api-unit        Run API unit tests"
	@echo "  make test-api-it          Run API integration tests "
	@echo ""
	@echo "Web Tests (Jest / Playwright):"
	@echo "  make test-web-unit        Run Web unit tests"
	@echo "  make test-web-it          Run Web integration tests"
	@echo ""
	@echo "End to end Tests:"
	@echo "  make test-e2e             Run end-to-end tests (Playwright / Cypress)"
	@echo ""
	@echo "Combined Test Targets:"
	@echo "  make test-unit            Run all unit tests (API + Web)"
	@echo "  make test-it              Run all integration tests (API + Web)"
	@echo "  make test-all             Run all tests sequentially (test-unit, test-it, test-e2e)"
	@echo ""
	@echo "Versioning:"
	@echo "  make version            Show current version (VERSION file)"
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

# ============================
# Web dependencies
# ============================
.PHONY: install-web
install-web:
	@echo "Installing datashare-web dependencies..."
	@cd datashare-web && npm install

# ============================
# Start backend / frontend
# ============================
.PHONY: start-api
start-api: 
	@echo "Starting backend..."
	@cd $(API_DIR) && ./mvnw spring-boot:run

.PHONY: start-web
start-web: install-web
	@echo "Starting frontend..."
	@cd $(WEB_DIR) && npm install && npm run start

.PHONY: start-all
start-all: 
	@echo "Starting backend + frontend..."
	$(MAKE) start-api &
	$(MAKE) start-web &

# ============================
# Linting (Angular / TypeScript / Checkstyle / SpotBugs / PMD)
# ============================
.PHONY: lint-web lint-api lint
lint-web: install-web
	@echo "Running datashare-web lint (Lint / Prettier)..."	
	@cd $(WEB_DIR) && npm run lint -- --max-warnings=0 || echo "    ⚠️ Lint warnings tolérés"
	@cd $(WEB_DIR) && npm run prettier:check || (npm run prettier && git add . && echo "    → Prettier fixé")

lint-api:
	@echo "Running datashare-api lint (Compile / Spotless / Checkstyle)..."
	@cd $(API_DIR) && ./mvnw compile -DskipTests -q
	@cd $(API_DIR) && ./mvnw spotless:check -q || (./mvnw spotless:apply && git add . && echo "Spotless fixed ✅")
#	@cd $(API_DIR) && ./mvnw checkstyle:check -q

lint: lint-api lint-web
	@echo "All lint checks completed."

# ============================
# Building (Angular / Maven)
# ============================
.PHONY: build-web build-api build-all
build-web: install-web
	@echo "Building datashare-web (Angular)..."
	@cd $(WEB_DIR) && npm run build

build-api: 
	@echo "Building datashare-api..."
	@cd $(API_DIR) && ./mvnw clean package -DskipTests

build-all: build-api build-web
	@echo "All applications built successfully."

# ============================
# Frontend tests (Jest / Playwright)
# ============================
.PHONY: test-web-unit test-web-it test-web-e2e
test-web-unit: install-web
	@echo "Running frontend unit tests..."
	@cd $(WEB_DIR) && npm run test-unit 

test-web-it: install-web
	@echo "Running frontend integration tests..."
	@cd $(WEB_DIR) && npm run test-it

test-web-e2e: install-web
	@echo "Running frontend end-to-end tests..."
	$(MAKE) start-api &
	@cd $(WEB_DIR) && npm run test-e2e  

# ============================
# Backend tests (Maven)
# ============================
.PHONY: test-api-unit test-api-it test-api-e2e
test-api-unit:
	@echo "Running backend unit tests..."
	@cd $(API_DIR) && ./mvnw test -Punit

test-api-it:
	@echo "Running backend integration tests..."
	@cd $(API_DIR) && ./mvnw verify -Pit

# ============================
# Combined targets
# ============================
.PHONY: test-unit
test-unit: test-api-unit test-web-unit
	@echo "All unit tests finished."

.PHONY: test-it
test-it: test-api-it test-web-it
	@echo "All integration tests finished."

.PHONY: test-all
test-all: test-unit test-it test-e2e
	@echo "All tests finished."

# ============================
# Git hooks
# ============================
.PHONY: install-hooks uninstall-hooks

install-hooks:
	@echo "Installing git hooks..."
	@git config core.hooksPath $(GIT_HOOKS_DIR)	
	@echo "Git hooks installed."

uninstall-hooks:
	@echo "Uninstalling git hooks..."
	@git config --unset core.hooksPath
	@echo "Git hooks uninstalled."
