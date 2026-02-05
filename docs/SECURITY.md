# Security

## Purpose

This document describes the security mechanisms implemented in the application.

---

## Authentication

### JWT Authentication

- Stateless authentication using JWT
- Email used as unique identifier
- Tokens are signed and time-limited
- Passwords hashed using BCrypt

---

## Authorization

- Role-based access control (extensible)
- Restricted access for anonymous users
- Secure endpoints protected by Spring Security

---

## Data Validation

### Client-Side

- Angular Reactive Forms
- Input validation before submission
- Immediate user feedback

### Server-Side

- Bean Validation (`@NotBlank`, `@Email`, `@Size`)
- Validation enforced at API boundaries
- Client input never trusted

---

## Secure File Access

- Download links contain:

  - single-use token
  - expiration date

- Tokens invalidated after use
- Files stored in private S3 buckets

---

## Error Handling

- Centralized error handling
- Standardized error responses
- No sensitive information exposed

---

## Infrastructure Security

- Secrets stored in environment variables
- No credentials in source code
- Docker images versioned and scanned

---

## Accessibility & Security

- Accessible authentication flows
- Clear and readable error messages
- Security mechanisms compatible with assistive technologies

---

### | [⬅ Back to DataShare README](../README.md) |
