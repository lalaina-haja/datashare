# Quality

## Purpose

This document defines quality standards and enforcement mechanisms.

---

## Quality Principles

* Maintainability
* Security
* Performance
* Accessibility
* Testability

---

## REST API Quality

* Consistent HTTP methods
* Proper HTTP status codes
* Clear request/response models

---

## Validation & Error Management

* Client-side and server-side validation
* Centralized error handling
* User-friendly error messages

---

## Code Formatting & Static Analysis

### Spotless

* Automatic code formatting
* Google Java Format
* Import ordering and cleanup

### Checkstyle

* Structural rules only:

  * method size
  * file size
  * cyclomatic complexity
* No formatting overlap with Spotless

---

## Automation

Makefile targets:
* `make lint-api` : Runs API lint (Compile /  Spotless / Checkstyle) 
* `make lint-web` : Runs Web lint (Angular Lint / Prettier) 
* `make lint`     : Run all lint checks (API + Web) 
	

---

## Accessibility as a Quality Requirement

* PSH accessibility treated as non-functional requirement
* Accessibility checks included in testing strategy

---

### | [⬅ Back to DataShare README](../README.md) |
