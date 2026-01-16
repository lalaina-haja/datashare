# Performance

## Purpose

This document defines performance objectives and optimization strategies.

---

## Performance Objectives

- REST API response time < 300 ms (average)
- Efficient file upload and download
- Scalability for concurrent users

---

## Backend Performance

- Stateless REST API (JWT)
- Connection pooling (HikariCP)
- Pagination for file history endpoints
- Streaming uploads/downloads to avoid memory overhead

---

## Database Performance

- Indexes on:
  - email
  - file_id
  - token

- Optimized queries

---

## File Storage Performance

- Use of pre-signed S3 URLs
- Backend not acting as file proxy
- Reduced network and memory usage

---

## Performance Testing

- Tools: Gatling / JMeter
- Scenarios:

  - concurrent uploads
  - token-based downloads

- Metrics:

  - response time
  - throughput
  - error rate

---

## Monitoring

- Spring Boot Actuator metrics
- Ready for Prometheus / Grafana integration

---

### | [⬅ Back to DataShare README](../README.md) |
