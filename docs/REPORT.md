# Research Summary (Spark + Elasticsearch + Secure Web)

## A) Current state assessment

### Apache Spark
- Architecture: driver + executors with DAG scheduling, resilient distributed datasets (RDD) / DataFrame API.
- Security features (baseline):
  - Authentication: supports Kerberos for cluster access.
  - Authorization: integration with Hadoop/YARN ACLs.
  - Encryption in transit: SSL/TLS between driver and executors.
  - Encryption at rest: relies on HDFS / storage layer policies.
  - Secret handling: Spark configs and environment variables.
- Security risks:
  - Misconfigured ACLs or open UIs expose job data.
  - Plaintext data in logs or shuffle if encryption disabled.

### Elasticsearch
- Architecture: distributed index with shards + replicas.
- Security features (baseline):
  - Authentication/authorization with built-in security (xpack) or proxy.
  - TLS for node-to-node and HTTP clients.
  - Audit logging (access and config changes).
  - API keys for service-to-service access.
- Security risks:
  - Default ports exposed to public network.
  - Missing TLS / auth causes data leakage.

## B) Integrated security model (3 layers)

1) Data processing layer (Spark)
   - Spark jobs only process encrypted metadata and sanitized fields.
   - Access to JDBC and Elasticsearch is controlled via environment secrets.
   - Spark job is separated from the web tier and executed by admin only.

2) Search/query layer (Elasticsearch)
   - Store only metadata (no raw medical content).
   - Index is refreshed via Spark batch job or backend reindex endpoint.
   - Access controlled by API key or local network.

3) Web layer (Spring Boot + React)
   - JWT authentication + RBAC for doctors, patients, admins.
   - Multi-level encryption: master key -> data key -> encrypted content (AES-GCM).
   - Metadata remains searchable and uses hashed identifiers (CCCD hash).
   - Audit log records every API request.
   - TLS ready with keystore config.

## C) Prototype system (implemented)

### Security & encryption
- AES-GCM for sensitive content.
- HMAC integrity check on encrypted payload.
- Master key management (create/rotate/backup).
- Data key per record (wrapped by master key).

### Authentication & authorization
- JWT authentication.
- Role-based access control (ADMIN, DOCTOR, PATIENT).
- Admin can manage users, roles, lock/unlock, and delete accounts.

### Logging & auditing
- Audit logs for every API request (username, role, endpoint, status, IP).
- Admin UI shows recent logs.

### Elasticsearch integration
- Backend service indexes metadata into Elasticsearch.
- Doctor UI can search by keyword (Elasticsearch) or by patient code.
- Admin can trigger reindex from database to Elasticsearch.

### Spark integration
- Spark job (`spark-jobs`) reads metadata from MySQL and reindexes to Elasticsearch.
- Backend supports running Spark via `spark-submit` when enabled.
- Job is optional and configurable in `application.yml`.

## How to run (demo)

1) Start infrastructure (Elasticsearch, Spark):
   - `docker-compose up -d`

2) Backend:
   - Configure `app.elasticsearch.enabled=true` in `application.yml`.
   - (Optional) Set `app.spark.enabled=true` and build spark job.

3) Spark job:
   - Build in `spark-jobs/` and run `spark-submit` (see `spark-jobs/README.md`).

## Notes and limitations
- Spark/Elasticsearch integration is focused on metadata indexing for privacy.
- Full TLS requires a real keystore (see `application.yml`).
- Elasticsearch security (xpack) can be enabled in production.

## D) Multi-level encryption policy
- PUBLIC: metadata only; sensitive content stays encrypted.
- CONFIDENTIAL: encrypted diagnosis visible to patient + doctor.
- RESTRICTED: encrypted diagnosis visible to doctor/admin only.
- Key hierarchy: master key -> data key -> encrypted payload (AES-GCM) + HMAC.

## E) Secure deployment options
- `docker-compose.secure.yml` enables TLS + auth for Elasticsearch and Spark.
- `application-secure.yml` enables HTTPS for the web layer and HTTPS for Elasticsearch.
- `ElasticsearchService` supports API key or Basic auth and CA trust.

## F) Evaluation
- See `docs/EVALUATION.md` for latency and security test steps.
