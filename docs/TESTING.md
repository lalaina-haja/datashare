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

- JUnit 5 + Mockito
- Business logic and validators
- Security-related rules

### Integration Tests

- Spring Boot Test
- PostgreSQL via Testcontainers
- LocalStack for S3
- JWT authentication flows tested

## Frontend

### Unit Tests

- Jest
- Component and service tests
- Form validation logic

### Integration Tests

Validate interaction between:

- components
- services
- routing
- HTTP layer

Tooling:

- Angular TestBed
- Angular Component Fixture

Tested Scenarios:

- Authentication flows (JWT handling)
- File upload and validation
- Secure download via token
- File history access
- Error handling and UX feedback

### Accessibility Testing (PSH)

- Keyboard navigation
- Screen reader compatibility
- ARIA attributes verification
- Lighthouse / Axe audits

### End-to-End Tests

- Cypress / Playwright
- Real user scenarios
- Isolated environment

### | [⬅ Back to DataShare README](../README.md) |
