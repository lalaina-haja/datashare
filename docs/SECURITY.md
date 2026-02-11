# Security

## Purpose
This document describes the security mechanisms implemented in the application.

## Authentication
- Cookies HttpOnly + Secure + SameSite=None
- No session storage / local storage
- Stateless authentication using JWT
- Email used as unique identifier
- Tokens are signed and time-limited
- Passwords hashed using BCrypt
- Restricted access for anonymous users
- Secure endpoints protected by Spring Security
- JWT authentication filter on server side
- Authentication interceptor on client side

## Data Validation
### Client-Side
- Angular Reactive Forms
- Input validation before submission
- Immediate user feedback

### Server-Side
- Bean Validation (`@NotBlank`, `@Email`, `@Size`)
- Validation enforced at API boundaries
- Client input never trusted

## Secure File Access
- Upload via presigned URL PUT
- Download via presigned URL GET
- Random token linked to the file
- Short presigned URL expiration (10 minutes)

## Error Handling
- Centralized error handling
- Standardized error responses
- No sensitive information exposed

## Infrastructure Security
- Secrets stored in environment variables
- No credentials in source code

## Accessibility & Security
- Accessible authentication flows
- Clear and readable error messages

---

### | [⬅ Back to DataShare README](../README.md) |
