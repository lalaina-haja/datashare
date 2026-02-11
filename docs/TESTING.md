# Testing

## Purpose

This document describes the testing strategy ensuring reliability and quality.

## Testing Strategy

The project follows the testing pyramid:

```
End-to-End
↑
Integration Tests
↑
Unit Tests
```

## Backend Tests

### Unit Tests
- Isolated tests on classes (no Spring context loaded)
- Using JUnit 5, Mockito, Maven Surefire plugin
- Business logic (services, validators, etc.)
- Security-related rules (JWT, cookies)

### Integration Tests
- Spring Boot Test (using a Spring test profile)
- H2 database and localstack via Testcontainers
- JWT authentication, all security flows tested
- Authentication and File Controllers 

## Frontend

### Unit Tests
- Isolated and mocked tests using Vitest
- Form validation logic
- Components and services 

### Integration Tests
- Validates interaction between components, services and routing
- HTTP layer
- Using vitest 

### End-to-End Tests
- Cypress + Cucumber
- Real user scenarios
- Isolated environment (docker, localstack)

### Accessibility Testing (PSH)
- Keyboard navigation
- Screen reader compatibility
- ARIA attributes verification
- Using WAVE (web accessibility evaluation tool)

### | [⬅ Back to DataShare README](../README.md) |
