# Changelog

## 1.0.0 - 2026-06-03

### Added

- Initial Spring Boot backend for secure hospital record management.
- React/Vite frontend for admin, doctor and patient workflows.
- JWT authentication and role-based access control.
- Encrypted medical record content with searchable metadata.
- Elasticsearch integration and optional Spark reindex job.
- Docker Compose infrastructure for Elasticsearch, Kibana and Spark.
- Security, evaluation, architecture and roadmap documentation.
- GitHub Actions CI workflow for backend, Spark job and frontend builds.
- GitHub issue templates and pull request checklist.

### Changed

- Public configuration now uses environment variables for credentials and runtime settings.

### Security

- Excluded generated certificates, keystores, logs, dependencies and build artifacts from Git.
