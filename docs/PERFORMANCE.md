# Performance

## Purpose

This document defines performance objectives and optimization strategies.


## Performance Objectives
- REST API response time < 300 ms (average)
- Efficient file upload and download
- Scalability for concurrent users

## Backend Performance
- Stateless REST API (JWT)

## Database Performance
- Indexes on:
  - email
  - file_id
  - token

## File Storage Performance
- Use of pre-signed S3 URLs
- Backend not acting as file proxy
- Reduced network and memory usage

## Performance Testing
- concurrent subscription & login
- concurrent presigned URL generation
- concurrent history requests

## Monitoring
- Spring Boot Actuator metrics 
- Ready for Prometheus / Grafana integration


### | [⬅ Back to DataShare README](../README.md) |
