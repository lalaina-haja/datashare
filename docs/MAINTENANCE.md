# MAINTENANCE

## Purpose
This document describes maintenance practices for datashare application.

## Updating dependencies
- Frequencies:
  - Every 6 weeks for frontend
  - Every 2 months for backend
  - Every 2 months for docker images

- Risks:
  - Angular: breaking major changes
  - Spring boot: security updates
  - AWS: changes on presigned URL

- Commands :
```
npm outdated
npm update
ng update

mvn versions:display-dependency-updates 
mvn versions:use-latest-releases
```
After updates, run all tests (unit, integration, e2e).

## Monitoring and debbuging
- sf4j structured logs
- Spring boot actuator
- DevTools

## Deployment & Maintenance Scripts
- Docker Compose for local development
- Environment configuration via `.env`
- Makefile for common tasks (build, format, quality). See  [Quick reference guide](docs/QUICK-REFERENCE.md)
- Custom scripts :
  | Scripts | Description |
  | --- | --- |
  | update-web-env | Update Angular environments |
  | bum-version | Bump Application version using semantic versionning |
  | git-hooks | lint on git commit and unit tests on git push  |
  | create-s3-bucket | Create localstack S3 bucket and configure CORS |
