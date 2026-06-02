# Evaluation Plan

This document lists the evaluation steps to validate performance and security.

## 1) Search latency (Elasticsearch vs DB fallback)

1. Enable Elasticsearch (`app.elasticsearch.enabled=true`).
2. Run the benchmark script with a doctor token:

```powershell
$env:AUTH_TOKEN="your_jwt_here"
./scripts/benchmark-search.ps1 -BaseUrl http://localhost:8080 -Keyword "flu" -Iterations 50
```

3. Disable Elasticsearch (`app.elasticsearch.enabled=false`) and rerun.

Record median and p95 to compare.

## 2) Encryption overhead

Measure average API time for:
- `POST /api/doctor/record`
- `PUT /api/doctor/record/{id}`

Repeat 30-50 runs and compare with/without encryption enabled (if you add a toggle).

## 3) Security checks

- RBAC: verify unauthorized roles get HTTP 403.
- JWT: verify expired tokens are rejected.
- Audit logs: verify requests are recorded in `/api/admin/audit-logs`.
- Data at rest: verify encrypted fields do not appear in plaintext in DB.
