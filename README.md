# Demo Apache Spark Elasticsearch - Secure Hospital Records

Full-stack hospital record management demo built with Spring Boot, React, Apache Spark, Elasticsearch and MySQL. The project focuses on secure medical data workflows: role-based access control, encrypted record content, searchable metadata indexing and Spark-based batch reindexing.

## Highlights

- JWT authentication with ADMIN, DOCTOR and PATIENT roles.
- Secure medical record flow with AES-GCM encryption and key wrapping.
- Doctor and patient dashboards for profile, record and history management.
- Elasticsearch metadata search for fast doctor-side patient/record lookup.
- Apache Spark job for batch reindexing medical metadata from MySQL to Elasticsearch.
- Admin features for user management, audit logs, master key lifecycle and search reindex status.
- Docker Compose setup for Elasticsearch, Kibana and Spark master/worker.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite |
| Data | MySQL, Elasticsearch |
| Processing | Apache Spark 3 |
| Tooling | Maven, Docker Compose, PowerShell scripts |

## Project Structure

```text
.
├── src/main/java/com/yourname/hospital   # Spring Boot backend
├── src/main/resources                    # Application configuration
├── frontend                              # React/Vite frontend
├── spark-jobs                            # Spark reindex job
├── scripts                               # Benchmark, data generation and Spark wrappers
├── spark-conf                            # Spark configuration templates
├── docs                                  # Report, evaluation and security setup notes
└── docker-compose.yml                    # Elasticsearch, Kibana and Spark services
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 18+
- Docker Desktop
- MySQL running locally or in Docker

### 1. Configure Environment

Copy `.env.example` and use the values as environment variables for your local shell. At minimum, configure:

```bash
DB_URL=jdbc:mysql://127.0.0.1:3307/hospitaldb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=change_me
JWT_SECRET=ChangeThisJwtSecretToAtLeast32CharsLong
```

### 2. Start Infrastructure

```bash
docker compose up -d
```

This starts Elasticsearch, Kibana and a local Spark cluster.

### 3. Run Backend

```bash
mvn spring-boot:run
```

Backend runs at `http://localhost:8081`.

### 4. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at the Vite URL printed in the terminal.

### 5. Build Spark Job

```bash
cd spark-jobs
mvn -q -DskipTests package
```

The job artifact is generated at `spark-jobs/target/record-reindex-job.jar`.

## Key API Areas

- `POST /api/auth/login` - login and receive JWT.
- `POST /api/auth/register` - register a patient account.
- `/api/admin/**` - user management, audit logs, master key and reindex operations.
- `/api/doctor/**` - patient search, medical record CRUD and doctor profile.
- `/api/patient/**` - patient profile, record history and password management.

## Documentation

- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Contributing](CONTRIBUTING.md)
- [Changelog](CHANGELOG.md)
- [Security setup](docs/SECURITY_SETUP.md)
- [Evaluation notes](docs/EVALUATION.md)
- [Research summary](docs/REPORT.md)
- [Spark job details](spark-jobs/README.md)

## Security Notes

This repository intentionally excludes generated certificates, keystores, logs, dependency folders and build artifacts. Use `.env.example` as a template and keep real credentials outside Git.
