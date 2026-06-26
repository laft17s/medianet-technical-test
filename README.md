<p align="center">
  <a href="./README_ES.md">
    <img src="https://img.shields.io/badge/lang-Espa%C3%B1ol-green.svg" alt="Versión en Español">
  </a>
</p>

<!-- GitHub renders the centered logo below. Cursor/VS Code preview often skips HTML blocks; use ![Laft Logo](./rsrc/laft-logo.svg) in the editor. -->
<p align="center">
  <img src="./rsrc/laft-logo.svg" width="200" alt="Laft Logo">
</p>

<br/>

# Medianet Technical Test — Core Banking Platform

Full-stack core banking solution for managing clients, accounts and movements. Built with Spring Boot microservices, gRPC, Kafka, PostgreSQL and Angular 13.

## Architecture

| Component | Description | Port |
|-----------|-------------|------|
| `ms-core-client-person` | Client/Person domain (gRPC) | 8081 / 9091 |
| `ms-core-account-movement` | Account & Movement domain (gRPC + Kafka) | 8082 / 9093 |
| `ms-comp-remittances` | REST gateway (WebFlux) | 8080 |
| `app-remittance` | Angular SPA | 4200 |
| PostgreSQL | 3 schemas: `db-clients`, `db-accounts`, `db-movements` | 5432 |
| Kafka + Zookeeper | Async movement events | 9092 / 2181 |

Internal communication uses **gRPC**. The composite exposes **REST + JWT** to the frontend.

### Design decisions

- Hexagonal architecture (ports & adapters) in core microservices
- WebFlux only in the composite layer
- Kafka for high-volume movement events and async account provisioning
- Centralized exceptions in `commons-dependencies`
- Role-based UI: clients see only their data; admins manage all records but cannot perform deposits/withdrawals

## Prerequisites

- JDK 17+, Maven 3.8+, Docker Desktop, Node 16+ (for local frontend dev)

## Quick start (Docker)

```bash
chmod +x deploy.sh
./deploy.sh --skip-tests
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| API / Swagger | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Default DB credentials are in `.env.example` (copy to `.env` before first run).

### Demo credentials (seed data)

| Role | Identification | Password |
|------|----------------|----------|
| Admin | `1311322422` | `admin123` |
| User (Jose Lema) | `1710034065` | `1234` |
| User (Marianela Montalvo) | `0910000017` | `5678` |
| User (Juan Osorio) | `1720000007` | `1245` |

### Configuration templates

Sensitive files are gitignored. Copy templates before running:

```bash
cp .env.example .env
cp docker-compose.yml.template docker-compose.yml
cp backend/composite/ms-comp-remittances/src/main/resources/application.properties.template \
   backend/composite/ms-comp-remittances/src/main/resources/application.properties
# repeat for other *.template files
```

## Local development

```bash
# Backend
cd backend && mvn clean install

# Frontend
cd frontend/app-remittance && npm install && npm start
```

## API overview

| Resource | Endpoints |
|----------|-----------|
| Auth | `POST /auth/login`, `POST /auth/register` |
| Clients | `GET/POST/PUT/DELETE /clients`, `PATCH /clients/{id}/status` |
| Accounts | `GET/POST/PUT/DELETE /accounts` |
| Movements | `GET/POST/PUT/DELETE /movements` |
| Reports | `GET /reports?fecha={start,end}&cliente={id}` |

## Testing

```bash
cd backend && mvn clean test
```

### Postman

Import [`postman_collection.json`](postman_collection.json) into Postman. Run **Auth → Login (User — Jose Lema)** first (`1710034065` / `1234`); the test script stores the JWT in the collection variable `token` for protected endpoints. All responses follow the `ApiResponse` envelope (`$.data` for payloads, `$.data.token` on login).

See [docs/TEST_COVERAGE.md](docs/TEST_COVERAGE.md) for JaCoCo report locations.

### Load testing (JMeter)

**Prerequisites:** stack running (`./deploy.sh --skip-tests`), API reachable on `:8080`, and JMeter installed at `tools/apache-jmeter-5.6.3/` (see [tools/README.md](tools/README.md)).

| Mode | Command | Notes |
|------|---------|-------|
| GUI | `./start-jmeter.sh` → **Start** | Plan `load_test.jmx` auto-loads; **Setup Thread Group** logs in as `1710034065` / `1234` and fetches JWT |
| CLI + HTML report (recommended) | `./scripts/run-load-test.sh` | Opens `docs/jmeter/report/index.html` |

**Artifacts**

| Path | Description |
|------|-------------|
| `docs/jmeter/EVIDENCE.md`, `docs/jmeter/statistics.json` | Committed evidence |
| `docs/jmeter/report/` | Generated locally (gitignored) |

**Troubleshooting:** if requests show `Authorization: Bearer 1`, reload `load_test.jmx` — the JSON extractor path must be `$.data.token`. Confirm the API is up on port 8080 before running.

Full guide: [docs/jmeter/LOAD_TEST.md](docs/jmeter/LOAD_TEST.md)

## AI-assisted development

Evidence folder: `ai/`

| File | Content |
|------|---------|
| `ai/prompts.md` | Prompt summary |
| `ai/decisions.md` | Accepted/rejected AI outputs |
| `ai/generations/` | Code fragments |
| `ai/agent/` | Scaffolding skills |

## Deliverables checklist

- [x] `database/BaseDatos.sql`
- [x] Postman collection (`postman_collection.json`)
- [x] Swagger UI + OpenAPI at `/v3/api-docs`
- [x] Angular frontend
- [x] Dockerfiles + docker-compose
- [x] Unit & integration tests + JaCoCo
- [x] JMeter plan (`load_test.jmx`) + evidence (`docs/jmeter/EVIDENCE.md`)
- [x] AI evidence folder

## Non-functional considerations

- **Performance**: gRPC internal calls, Kafka async processing for movements
- **Scalability**: Stateless microservices, independent DB schemas
- **Resilience**: Health checks in docker-compose, graceful gRPC error mapping
