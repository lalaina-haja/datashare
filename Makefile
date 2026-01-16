# ============================
# Variables
# ============================
SHELL := /bin/bash
VERSION := $(shell cat VERSION)
API_DIR := datashare-api
WEB_DIR := datashare-web
GIT_HOOKS_DIR := scripts/git-hooks

# Define colors
RED    := \033[0;31m
GREEN  := \033[0;32m
YELLOW := \033[0;33m
BLUE   := \033[0;34m
NC     := \033[0m  # No Color

# ============================
# Help
# ============================
.PHONY: help
help:
	@echo ""
	@echo -e "$(GREEN)DataShare Monorepo Makefile"
	@echo ""
	@echo -e "$(BLUE)Applications:"
	@echo -e "$(YELLOW)  make start-api            $(NC)Start datashare-api (Spring Boot dev server)"
	@echo -e "$(YELLOW)  make start-web            $(NC)Start datashare-web (Angular dev server)"
	@echo -e "$(YELLOW)  make start-all            $(NC)Start API + Web"
	@echo ""
	@echo -e "$(BLUE)Build:"
	@echo -e "$(YELLOW)   make build-api            $(NC)Build the API (Spring Boot package)"
	@echo -e "$(YELLOW)   make build-web            $(NC)Build the Web (Angular build)"
	@echo -e "$(YELLOW)   make build-all            $(NC)Build both API and Web"
	@echo ""
	@echo -e "$(BLUE)Web dependencies:"
	@echo -e "$(YELLOW)   make install-web          $(NC)Install all Web dependencies"
	@echo ""
	@echo -e "$(BLUE)Linting:"
	@echo -e "$(YELLOW)   make lint-api             $(NC)Run API lint (Compile / Spotless / Checkstyle)"
	@echo -e "$(YELLOW)   make lint-web             $(NC)Run Web lint (Angular Lint / Prettier)"
	@echo -e "$(YELLOW)   make lint                 $(NC)Run all lint checks (API + Web)"
	@echo ""
	@echo -e "$(BLUE)API Tests (Maven profiles):"
	@echo -e "$(YELLOW)   make test-api-unit        $(NC)Run API unit tests"
	@echo -e "$(YELLOW)   make test-api-it          $(NC)Run API integration tests "
	@echo ""
	@echo -e "$(BLUE)Web Tests (Jest / Playwright):"
	@echo -e "$(YELLOW)   make test-web-unit        $(NC)Run Web unit tests"
	@echo -e "$(YELLOW)   make test-web-it          $(NC)Run Web integration tests"
	@echo ""
	@echo -e "$(BLUE)End to end Tests:"
	@echo -e "$(YELLOW)   make test-e2e             $(NC)Run end-to-end tests (Playwright / Cypress)"
	@echo ""
	@echo -e "$(BLUE)Combined Test Targets:"
	@echo -e "$(YELLOW)   make test-unit            $(NC)Run all unit tests (API + Web)"
	@echo -e "$(YELLOW)   make test-it              $(NC)Run all integration tests (API + Web)"
	@echo -e "$(YELLOW)   make test-all             $(NC)Run all tests sequentially (test-unit, test-it, test-e2e)"
	@echo ""
	@echo -e "$(BLUE)Versioning:"
	@echo -e "$(YELLOW)   make version              $(NC)Show current version (VERSION file)"
	@echo -e "$(YELLOW)   make bump-version         $(NC)Bump version (TYPE=patch|minor|major)"
	@echo ""
	@echo -e "$(BLUE)Git hooks:"
	@echo -e "$(YELLOW)   make install-hooks        $(NC)Install git hooks"
	@echo -e "$(YELLOW)   make uninstall-hooks      $(NC)Uninstall git hooks"
	@echo ""
	@echo -e "$(BLUE)Run a custom command from API or WEB folder:"
	@echo -e "$(YELLOW)   make run-api CMD=\"command\" $(NC)Run command from API folder (CMD=\"command --arg\")"
	@echo -e "$(YELLOW)   make run-web CMD=\"command\" $(NC)Run command from API folder (CMD=\"command --arg\")"
	@echo -e "$(NC)"

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
	@echo -e "$(GREEN)[make] $(YELLOW)Bumping version ($(TYPE))...$(NC)"
	$(BUMP_SCRIPT) $(TYPE)
	@echo -e "$(GREEN)[make] $(YELLOW)New version: $$(cat $(VERSION_FILE))$(NC)"

# ============================
# Web dependencies
# ============================
.PHONY: install-web
install-web:
	@echo -e "$(GREEN)[make] $(YELLOW)Installing datashare-web dependencies...$(NC)"
	@cd datashare-web && npm install

# ============================
# Start backend / frontend
# ============================
.PHONY: start-api
start-api: 
	@echo -e "$(GREEN)[make] $(YELLOW)Starting backend...$(NC)"
	@cd $(API_DIR) && ./mvnw spring-boot:run

.PHONY: start-web
start-web: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Starting frontend...$(NC)"
	@cd $(WEB_DIR) && npm install && npm run start

.PHONY: start-all
start-all: 
	@echo -e "$(GREEN)[make] $(YELLOW)Starting backend + frontend...$(NC)"
	$(MAKE) start-api &
	$(MAKE) start-web &

# ============================
# Linting (Angular / TypeScript / Checkstyle / SpotBugs / PMD)
# ============================
.PHONY: lint-web lint-api lint
lint-web: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Running datashare-web lint (Lint / Prettier)...$(NC)"	
	@cd $(WEB_DIR) && npm run lint -- --max-warnings=0 || echo -e "$(YELLOW)    ⚠️ Lint warnings tolérés$(NC)"
	@cd $(WEB_DIR) && npm run prettier:check || (npm run prettier && git add . && echo -e "$(YELLOW)    → Prettier fixé$(NC)")

lint-api:
	@echo -e "$(GREEN)[make] $(YELLOW)Running datashare-api lint (Compile / Spotless / Checkstyle)...$(NC)"
	@cd $(API_DIR) && ./mvnw compile -DskipTests -q
	@cd $(API_DIR) && ./mvnw spotless:check -q || (./mvnw spotless:apply && git add . && echo -e "$(YELLOW)Spotless fixed ✅$(NC)")

lint: lint-api lint-web
	@echo -e "$(GREEN)[make] $(YELLOW)All lint checks completed.$(NC)"

# ============================
# Building (Angular / Maven)
# ============================
.PHONY: build-web build-api build-all
build-web: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Building datashare-web (Angular)...$(NC)"
	@cd $(WEB_DIR) && npm run build

build-api: 
	@echo -e "$(GREEN)[make] $(YELLOW)Building datashare-api...$(NC)"
	@cd $(API_DIR) && ./mvnw clean package -DskipTests

build-all: build-api build-web
	@echo -e "$(GREEN)[make] $(YELLOW)All applications built successfully.$(NC)"
# ============================
# Frontend tests (Jest / Playwright)
# ============================
.PHONY: test-web-unit test-web-it test-web-e2e
test-web-unit: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Running frontend unit tests...$(NC)"
	@cd $(WEB_DIR) && npm run test-unit 

test-web-it: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Running frontend integration tests...$(NC)"
	@cd $(WEB_DIR) && npm run test-it

test-web-e2e: install-web
	@echo -e "$(GREEN)[make] $(YELLOW)Running frontend end-to-end tests...$(NC)"
	$(MAKE) start-api &
	@cd $(WEB_DIR) && npm run test-e2e  

# ============================
# Backend tests (Maven)
# ============================
.PHONY: test-api-unit test-api-it test-api-e2e
test-api-unit:
	@echo -e "$(GREEN)[make] $(YELLOW)Running backend unit tests...$(NC)"
	@cd $(API_DIR) && ./mvnw test -Punit

test-api-it:
	@echo -e "$(GREEN)[make] $(YELLOW)Running backend integration tests...$(NC)"
	@cd $(API_DIR) && ./mvnw verify -Pit

# ============================
# Combined targets
# ============================
.PHONY: test-unit
test-unit: test-api-unit test-web-unit
	@echo -e "$(GREEN)[make] $(YELLOW)All unit tests finished.$(NC)"

.PHONY: test-it
test-it: test-api-it test-web-it
	@echo -e "$(GREEN)[make] $(YELLOW)All integration tests finished.$(NC)"
.PHONY: test-all
test-all: test-unit test-it test-e2e
	@echo -e "$(GREEN)[make] $(YELLOW)All tests finished.$(NC)"

# ============================
# Git hooks
# ============================
.PHONY: install-hooks uninstall-hooks

install-hooks:
	@echo -e "$(GREEN)[make] $(YELLOW)Installing git hooks...$(NC)"
	@git config core.hooksPath $(GIT_HOOKS_DIR)	
	@echo -e "$(GREEN)[make] $(YELLOW)Git hooks installed.$(NC)"

uninstall-hooks:
	@echo -e "$(GREEN)[make] $(YELLOW)Uninstalling git hooks...$(NC)"
	@git config --unset core.hooksPath
	@echo -e "$(GREEN)[make] $(YELLOW)Git hooks uninstalled.$(NC)"

# ============================
# Run a custom command from API or Web folder
# ============================
.PHONY: run-api
run-api:
	@echo -e "$(GREEN)[make] $(YELLOW)Running command from datashare-api folder: CMD=$(CMD)$(NC)"
	@cd $(API_DIR) && $(CMD)

.PHONY: run-web
run-web:
	@echo -e "$(GREEN)[make] $(YELLOW)Running command from datashare-web folder: CMD=$(CMD)$(NC)"
	@cd $(WEB_DIR) && $(CMD)
