# Contributing

## Development Workflow

1. Create a branch from `main`.
2. Keep changes focused on one feature, fix or documentation update.
3. Run the relevant build before opening a pull request.
4. Update documentation when behavior or setup changes.

## Branch Naming

- `feature/<name>` for new functionality.
- `fix/<name>` for bug fixes.
- `docs/<name>` for documentation updates.
- `chore/<name>` for tooling and repository maintenance.

## Local Checks

Backend:

```bash
mvn -DskipTests package
```

Spark job:

```bash
cd spark-jobs
mvn -DskipTests package
```

Frontend:

```bash
cd frontend
npm install
npm run build
```

## Security

Do not commit generated certificates, keystores, logs, dependency folders, build outputs or real credentials. Use `.env.example` as the template for local configuration.
