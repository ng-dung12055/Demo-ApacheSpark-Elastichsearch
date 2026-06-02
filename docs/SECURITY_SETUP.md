# Security Setup (TLS + Auth)

This guide enables TLS/auth for Elasticsearch and Spark in the demo stack.

## 1) Generate certificates

Use the PowerShell script (requires Docker + keytool):

```powershell
./scripts/generate-dev-certs.ps1
```

Outputs:
- `certs/ca/ca.crt`
- `certs/es01/es01.crt` and `certs/es01/es01.key`
- `certs/spark-keystore.jks`
- `certs/spark-truststore.jks`

## 2) Start secure infrastructure

```bash
docker-compose -f docker-compose.secure.yml up -d
```

This enables:
- Elasticsearch TLS + basic auth (user: `elastic`, password from `ES_SECURE_PASSWORD`)
- Spark RPC authentication + encryption + TLS

## 3) Run backend with TLS

Use the secure profile config:

```bash
SPRING_CONFIG_NAME=application-secure ./mvnw spring-boot:run
```

If you use self-signed certs for Elasticsearch, either:
- import the CA into a JVM truststore, or
- set `app.elasticsearch.ca-cert-path` (see `application-secure.yml`).

## 4) Spark job with secured Elasticsearch

The reindex job supports TLS + basic auth:

```bash
export ES_URL="https://localhost:9200"
export ES_USER="elastic"
export ES_PASSWORD="change_me"
export ES_CA_CERT="./certs/ca/ca.crt"
```

Then run the job (see `spark-jobs/README.md`).
