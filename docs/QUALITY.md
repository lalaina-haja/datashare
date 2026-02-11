# Quality

## Purpose
This document defines quality standards and enforcement mechanisms.

## Quality Principles
* Maintainability
* Security
* Performance
* Accessibility
* Testability

## REST API Quality
* Consistent HTTP methods
* Proper HTTP status codes
* Clear request/response models
* OpenAPI specification provided

## Validation & Error Management
* Client-side and server-side validation
* Centralized error handling
* User-friendly error messages

## Code Formatting 
Standard rules applied with :
- Spotless on backend
- ESLint and prettier on frondend

## Automation
Makefile targets:
* `make lint-api` : Runs API lint (Compile /  Spotless) 
* `make lint-web` : Runs Web lint (ESLint / Prettier) 
* `make lint`     : Run all lint checks (API + Web) 

Git hooks:
* `pre-commit` : Runs make lint
* `pre-push`   : Runs make test-unit 

## Accessibility as a Quality Requirement
* PSH accessibility treated as non-functional requirement
* Accessibility checks included in testing strategy

---

### | [⬅ Back to DataShare README](../README.md) |
