# ============================
# Variables
# ============================
SHELL := /bin/bash
VERSION := $(shell cat VERSION)
API_DIR := datashare-api
WEB_DIR := datashare-web
GIT_HOOKS_DIR := scripts/git-hooks

# Load .env if exists
ifneq (,$(wildcard .env))
    include .env
    export
endif

# Default API port
API_PORT ?= 8080

# Define colors
COLOR_TITLE    := \033[1;92m # High Intensity Green
COLOR_KEYWORD  := \033[0;96m # Cyan
COLOR_MESSAGE  := \033[1;37m # White
COLOR_NOCOLOR  := \033[0m  # No Color

# Text Format
F_MESSAGE := $(COLOR_NOCOLOR)[$(COLOR_TITLE)make$(COLOR_NOCOLOR)] $(COLOR_MESSAGE)
F_TITLE   := $(COLOR_NOCOLOR)$(COLOR_TITLE)
F_REGULAR := $(COLOR_NOCOLOR)
F_KEYWORD := $(COLOR_NOCOLOR)$(COLOR_KEYWORD)

# ============================
# Help
# ============================
.PHONY: help
help:
	@echo ""
	@echo -e "$(F_MESSAGE)DataShare Monorepo Makefile"
	@echo ""
	@echo -e "$(F_TITLE)Applications:                        "
	@echo -e "$(F_KEYWORD)  make start-api            $(F_REGULAR)Start datashare-api (Spring Boot dev server)"
	@echo -e "$(F_KEYWORD)  make start-web            $(F_REGULAR)Start datashare-web (Angular dev server)"
	@echo -e "$(F_KEYWORD)  make start-all            $(F_REGULAR)Start API + Web"
	@echo ""
	@echo -e "$(F_TITLE)Build:"
	@echo -e "$(F_KEYWORD)   make build-api            $(F_REGULAR)Build the API (Spring Boot package)"
	@echo -e "$(F_KEYWORD)   make build-web            $(F_REGULAR)Build the Web (Angular build)"
	@echo -e "$(F_KEYWORD)   make build-all            $(F_REGULAR)Build both API and Web"
	@echo ""
	@echo -e "$(F_TITLE)Web dependencies:"
	@echo -e "$(F_KEYWORD)   make install-web          $(F_REGULAR)Install all Web dependencies"
	@echo ""
	@echo -e "$(F_TITLE)Linting:"
	@echo -e "$(F_KEYWORD)   make lint-api             $(F_REGULAR)Run API lint (Compile / Spotless)"
	@echo -e "$(F_KEYWORD)   make lint-web             $(F_REGULAR)Run Web lint (ESLint / Prettier)"
	@echo -e "$(F_KEYWORD)   make lint                 $(F_REGULAR)Run all lint checks (API + Web)"
	@echo ""
	@echo -e "$(F_TITLE)API Tests (Maven Surefire / Failsafe):"
	@echo -e "$(F_KEYWORD)   make test-api-unit        $(F_REGULAR)Run API unit tests"
	@echo -e "$(F_KEYWORD)   make test-api-integ       $(F_REGULAR)Run API integration tests "
	@echo ""
	@echo -e "$(F_TITLE)Web Tests (Vitest):"
	@echo -e "$(F_KEYWORD)   make test-web-unit        $(F_REGULAR)Run Web unit tests"
	@echo -e "$(F_KEYWORD)   make test-web-integ       $(F_REGULAR)Run Web integration tests"
	@echo ""
	@echo -e "$(F_TITLE)End to end Tests (Cypress):"
	@echo -e "$(F_KEYWORD)   make test-e2e-open        $(F_REGULAR)Run end-to-end tests in GUI mode (Cypress open)"
	@echo -e "$(F_KEYWORD)   make test-e2e-ci          $(F_REGULAR)Run end-to-end tests in headless mode (Cypress run)"
	@echo ""
	@echo -e "$(F_TITLE)Combined Test Targets:"
	@echo -e "$(F_KEYWORD)   make test-unit            $(F_REGULAR)Run all unit tests in ci mode (API + Web)"
	@echo -e "$(F_KEYWORD)   make test-integ           $(F_REGULAR)Run all integration tests in ci mode (API + Web)"
	@echo -e "$(F_KEYWORD)   make test-all             $(F_REGULAR)Run all tests sequentially in ci mode (test-unit, test-integ, test-e2e)"
	@echo ""
	@echo -e "$(F_TITLE)Versioning:"
	@echo -e "$(F_KEYWORD)   make version              $(F_REGULAR)Show current version (VERSION file)"
	@echo -e "$(F_KEYWORD)   make bump-version         $(F_REGULAR)Bump version (TYPE=patch|minor|major)"
	@echo ""
	@echo -e "$(F_TITLE)Git hooks:"
	@echo -e "$(F_KEYWORD)   make install-hooks        $(F_REGULAR)Install git hooks"
	@echo -e "$(F_KEYWORD)   make uninstall-hooks      $(F_REGULAR)Uninstall git hooks"
	@echo ""
	@echo -e "$(F_TITLE)Documentation:"
	@echo -e "$(F_KEYWORD)   make doc-api              $(F_REGULAR)Backend API documentation (Javadoc)"
	@echo -e "$(F_KEYWORD)   make doc-web              $(F_REGULAR)Frontend API documentation (Typedoc)"
	@echo -e "$(F_KEYWORD)   make doc                  $(F_REGULAR)Combined documentation (Javadoc + Typedoc)"
	@echo -e "$(F_KEYWORD)   make publish-doc          $(F_REGULAR)Git commit updated docs"
	@echo ""
	@echo -e "$(F_TITLE)Run a custom command from API or WEB folder:"
	@echo -e "$(F_KEYWORD)   make run-api CMD=\"command\" $(F_REGULAR)Run command from API folder (CMD=\"command --arg\")"
	@echo -e "$(F_KEYWORD)   make run-web CMD=\"command\" $(F_REGULAR)Run command from API folder (CMD=\"command --arg\")"
	@echo -e "$(F_REGULAR)"

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
	@echo -e "$(F_MESSAGE)Bumping version ($(TYPE))...$(F_REGULAR)"
	$(BUMP_SCRIPT) $(TYPE)
	@echo -e "$(F_MESSAGE) ✔ New version: $$(cat $(VERSION_FILE))$(F_REGULAR)"

# ============================
# Web dependencies
# ============================
.PHONY: install-web
install-web:
	@echo -e "$(F_MESSAGE)Installing datashare-web dependencies...$(F_REGULAR)"
	@cd ${WEB_DIR} && npm install

# ============================
# Start backend / frontend
# ============================
.PHONY: start-api
start-api: 
	@if nc -z localhost $(API_PORT); then \
		echo -e "$(F_MESSAGE)Backend is already running.$(F_REGULAR)"; \
	else \
		( echo -e "$(F_MESSAGE)Starting backend...$(F_REGULAR)"; cd $(API_DIR) && ./mvnw spring-boot:run ) \
	fi

.PHONY: start-web
start-web: install-web
	@echo -e "$(F_MESSAGE)Starting frontend...$(F_REGULAR)"
	@source .env && cd $(WEB_DIR) && npm run start

.PHONY: start-all
start-all: 
	@echo -e "$(F_MESSAGE)Starting backend + frontend...$(F_REGULAR)"
	$(MAKE) start-api &
	$(MAKE) start-web &

# ============================
# Linting (ESLint / Prettier / Spotless )
# ============================
.PHONY: lint-web lint-api lint
lint-web: install-web
	@echo -e "$(F_MESSAGE)Running datashare-web lint (Lint / Prettier)...$(F_REGULAR)"	
	@cd $(WEB_DIR) && npm run lint -- --max-warnings=0 || echo -e "$(F_MESSAGE) Lint warnings tolerated$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run format:check || (npm run format && echo -e "$(F_MESSAGE) ✔ Prettier fixed$(F_REGULAR)")
lint-api:
	@echo -e "$(F_MESSAGE)Running datashare-api lint (Compile / Spotless)...$(F_REGULAR)"
	@cd $(API_DIR) && ./mvnw compile -DskipTests -q
	@cd $(API_DIR) && ./mvnw spotless:check -q || (./mvnw spotless:apply && echo -e "$(F_MESSAGE) ✔ Spotless fixed$(F_REGULAR)")
lint: lint-api lint-web
	@echo -e "$(F_MESSAGE) ✔ All lint checks completed.$(F_REGULAR)"

# ============================
# Building (Angular / Maven)
# ============================
.PHONY: build-web build-api build-all
build-web: install-web
	@echo -e "$(F_MESSAGE)Building datashare-web (Angular)...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run build

build-api: 
	@echo -e "$(F_MESSAGE)Building datashare-api...$(F_REGULAR)"
	@cd $(API_DIR) && ./mvnw clean package -DskipTests

build-all: build-api build-web
	@echo -e "$(F_MESSAGE) ✔ All applications built successfully.$(F_REGULAR)"

# ============================
# Backend tests (Maven Surefire / Failsafe)
# ============================
.PHONY: test-api-unit 
test-api-unit:
	@echo -e "$(F_MESSAGE)Running backend unit tests...$(F_REGULAR)"
	@cd $(API_DIR) && ./mvnw test -Punit

.PHONY: test-api-integ
test-api-integ:
	@echo -e "$(F_MESSAGE)Running backend integration tests...$(F_REGULAR)"
	@cd $(API_DIR) && ./mvnw verify -Pit -Dspring.profiles.active=test

# ============================
# Frontend tests (Vitest)
# ============================
.PHONY: test-web-unit 
test-web-unit: install-web
	@echo -e "$(F_MESSAGE)Running frontend unit tests...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:unit 

.PHONY: test-web-integ
test-web-integ: install-web
	@echo -e "$(F_MESSAGE)Running frontend integration tests...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:integ

# ============================
# End to End tests (Cypress)
# ============================
.PHONY: test-e2e-open 
test-e2e-open: install-web
	$(MAKE) start-api &
	@echo -e "$(F_MESSAGE)Running end-to-end tests in GUI mode...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:e2e:open

.PHONY: test-e2e-ci
test-e2e-ci: install-web
	$(MAKE) start-api &
	@echo -e "$(F_MESSAGE)Running end-to-end tests in headless mode...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:e2e:ci

# ============================
# Combined targets
# ============================
.PHONY: test-unit
test-unit: test-api-unit 
	@echo -e "$(F_MESSAGE)Running frontend unit tests...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:unit:ci
	@echo -e "$(F_MESSAGE) ✔ All unit tests finished.$(F_REGULAR)"

.PHONY: test-integ
test-integ: test-api-integ 
	@echo -e "$(F_MESSAGE)Running frontend integration tests...$(F_REGULAR)"
	@cd $(WEB_DIR) && npm run test:integ:ci
	@echo -e "$(F_MESSAGE) ✔ All integration tests finished.$(F_REGULAR)"

.PHONY: test-all
test-all: test-unit test-integ test-e2e-ci
	@echo -e "$(F_MESSAGE) ✔ All tests finished.$(F_REGULAR)"

# ============================
# Git hooks
# ============================
.PHONY: install-hooks uninstall-hooks

install-hooks:
	@echo -e "$(F_MESSAGE)Installing git hooks...$(F_REGULAR)"
	@git config core.hooksPath $(GIT_HOOKS_DIR)	
	@echo -e "$(F_MESSAGE)Git hooks installed.$(F_REGULAR)"

uninstall-hooks:
	@echo -e "$(F_MESSAGE)Uninstalling git hooks...$(F_REGULAR)"
	@git config --unset core.hooksPath
	@echo -e "$(F_MESSAGE)Git hooks uninstalled.$(F_REGULAR)"

# ============================
# Run a custom command from API or Web folder
# ============================
.PHONY: run-api
run-api:
	@echo -e "$(F_MESSAGE)Running command from datashare-api folder: CMD=$(CMD)$(F_REGULAR)"
	@cd $(API_DIR) && $(CMD)

.PHONY: run-web
run-web:
	@echo -e "$(F_MESSAGE)Running command from datashare-web folder: CMD=$(CMD)$(F_REGULAR)"
	@cd $(WEB_DIR) && $(CMD)

# ============================
# Generate documentation (Javadoc / Typedoc)
# ============================
.PHONY: doc-api doc-web doc publish-doc
doc-api:
	@mkdir -p docs/javadoc
	@cd $(API_DIR) && ./mvnw javadoc:javadoc
	@echo -e "$(F_MESSAGE)📚 Javadoc: docs/javadoc/index.html$(F_REGULAR)"
doc-web:
	@mkdir -p docs/typedoc
	@cd $(WEB_DIR) && npm run typedoc
	@echo -e "$(F_MESSAGE)📚 TypeDoc: docs/typedoc/index.html$(F_REGULAR)"
doc: doc-api doc-web
	@echo -e "$(F_MESSAGE)✔ Documentation complète: docs/$(F_REGULAR)"
publish-doc: doc
	@git add docs/
	@git commit -m "docs: update javadoc + typedoc" || echo "Rien à commiter$(F_REGULAR)"
	@echo -e "$(F_MESSAGE)✔ Docs versionnés Git !$(F_REGULAR)"