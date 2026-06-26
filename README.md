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

See [docs/TEST_COVERAGE.md](docs/TEST_COVERAGE.md) for JaCoCo report locations.

Load testing: [docs/jmeter/LOAD_TEST.md](docs/jmeter/LOAD_TEST.md)

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
- [x] Postman collections (`postman_collection.json`)
- [x] Swagger UI + OpenAPI at `/v3/api-docs`
- [x] Angular frontend
- [x] Dockerfiles + docker-compose
- [x] Unit & integration tests + JaCoCo
- [x] JMeter plan (`load_test.jmx`)
- [x] AI evidence folder

## Non-functional considerations

- **Performance**: gRPC internal calls, Kafka async processing for movements
- **Scalability**: Stateless microservices, independent DB schemas
- **Resilience**: Health checks in docker-compose, graceful gRPC error mapping
