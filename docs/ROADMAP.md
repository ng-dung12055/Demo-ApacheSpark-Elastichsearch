# Roadmap

## Completed

- Spring Boot backend with JWT authentication and role-based authorization.
- React frontend with admin, doctor and patient screens.
- Medical record encryption with key wrapping.
- Elasticsearch metadata indexing and search.
- Optional Spark reindex job for batch indexing.
- Audit log screen for admin users.
- Docker Compose setup for Elasticsearch, Kibana and Spark.
- CI workflow for backend, Spark job and frontend builds.

## Next Milestones

### Milestone 1: Demo Readiness

- Add sample screenshots for admin, doctor and patient workflows.
- Add seed data documentation for repeatable local demos.
- Add a short demo script for CV/interview walkthroughs.

### Milestone 2: API Quality

- Add OpenAPI documentation for auth, admin, doctor and patient endpoints.
- Add controller-level integration tests.
- Add validation error response examples.

### Milestone 3: Production Hardening

- Move secret management to a dedicated provider.
- Add stricter security headers and rate limiting.
- Add centralized structured logging.
- Add health checks for MySQL, Elasticsearch and Spark integration.

### Milestone 4: Search and Analytics

- Add search ranking and filters by diagnosis, department and visit date.
- Add Spark metrics export for reindex jobs.
- Add Kibana dashboard documentation for operational monitoring.
