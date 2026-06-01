# AncientData WebGIS — Backend

Spring Boot 3 REST API with PostGIS spatial data support, JWT authentication, and Flyway migrations.

- **Java 21**
- **Spring Boot 3.5.x**
- **PostgreSQL + PostGIS**
- **Gradle wrapper** (`./gradlew`)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java (JDK) | 21 |
| Gradle | via `./gradlew` (no install needed) |
| PostgreSQL | remote or local with PostGIS |

---

## Run Locally — Checklist

### 1. Create a `.env` file in the project root

```dotenv
DB_URL=jdbc:postgresql://<host>:<port>/<database>
DB_USER=<db_username>
DB_PASSWORD=<db_password>
JWT_SECRET=<base64-encoded-secret-min-32-chars>
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080
```

> ⚠️ This file is loaded automatically via `spring.config.import=optional:file:.env[.properties]`.  
> Never commit `.env` to version control.

### 2. Check nothing is already using port 8080

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
```

If another process (e.g. a leftover IDE run) is occupying the port, kill it first:

```bash
kill <PID>
```

### 3. Run with the `dev` profile

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Or from IntelliJ, use the **AncientData – Backend** run configuration (already configured with `dev` profile and `.env` path).

### 4. Verify the application is up

```bash
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP"}`

---

## Running Tests

```bash
./gradlew test
```

Test reports are written to `build/reports/tests/test/index.html`.

---

## Common Startup Errors

| Error | Cause | Fix |
|-------|-------|-----|
| `Could not resolve placeholder 'JWT_SECRET'` | `.env` not found or env var missing | Ensure `.env` exists in project root with all required vars |
| `JWT Secret is missing!` | `JWT_SECRET` is blank | Set a non-empty value in `.env` |
| `JWT expiration is invalid!` | `JWT_EXPIRATION` is 0 or missing | Set `JWT_EXPIRATION=86400000` (24h in ms) |
| `Port 8080 was already in use` | Another process (e.g. previous IDE run) is bound to 8080 | Run `lsof -nP -iTCP:8080 -sTCP:LISTEN` and kill the occupying PID |
| `UnsatisfiedDependencyException: jwtFilter → jwtUtil` | Caused by any of the above JWT misconfigs | Follow the JWT rows above |

---

## Docker

A `Dockerfile` and `docker-compose.yml` are included for containerised deployment.

### 1. Prepare `.env` for Docker Compose

Copy `.env.example` to `.env` and customize all values (especially passwords):

```bash
cp .env.example .env
```

Then edit `.env` with your own secrets:

```dotenv
# Backend
DB_URL=jdbc:postgresql://db:5432/webGIS_DB
DB_USER=webgis_client
DB_PASSWORD=your_strong_db_password
JWT_SECRET=your_base64_jwt_secret
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8080

# Docker services
POSTGRES_USER=root
POSTGRES_PASSWORD=your_postgres_password
POSTGRES_DB=webGIS_DB
GEOSERVER_ADMIN_PASSWORD=your_geoserver_password
PGADMIN_DEFAULT_EMAIL=your-email@example.com
PGADMIN_DEFAULT_PASSWORD=your_pgadmin_password
```

> ⚠️ **Never commit `.env` to version control.** The `.env` file is already listed in `.gitignore`.

### 2. Start services

```bash
docker compose up --build
```

Docker Compose will automatically read variables from `.env` and pass them to each service.

---

## Project Structure

```
docs/
  architecture/
    WORKFLOW-ANALYSIS.md
  ci-cd/
    CI-CD-DECISIONS.md
  security/
    SECURITY-UPGRADE-PROMPT.md
    2026-05/
      SECURITY-UPGRADE-STATUS.md
      DEPENDENCY-UPGRADE-SUMMARY.md
      TEST-REPORT.md
src/
  main/
    java/com/webgis/ancientdata/
      security/        # JWT filter, JwtUtil, security config
      ...
    resources/
      application.properties
  test/
    ...
.env                   # local secrets (not committed)
build.gradle
```

---

## License

Source code is licensed under the [Apache License 2.0](LICENSE).
Documentation, data, and media are licensed under [CC-BY-4.0](LICENSE-DOCS).

## Working with AI Agents in this repo

Agent guidance for this repo lives in `AGENTS.md` (the single source of truth: behavioral guidelines, hard rules, request template, definition of done, and API contract-change mode). For GitHub Copilot, `.github/copilot-instructions.md` points to that file.

