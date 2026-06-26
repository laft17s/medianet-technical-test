<p align="center">
  <a href="./README.md">
    <img src="https://img.shields.io/badge/lang-English-blue.svg" alt="English Version">
  </a>
</p>

<!-- GitHub muestra el logo centrado. La vista previa de Cursor/VS Code suele omitir HTML; use ![Laft Logo](./rsrc/laft-logo.svg) en el editor. -->
<p align="center">
  <img src="./rsrc/laft-logo.svg" width="200" alt="Laft Logo">
</p>

<br/>

# Medianet Technical Test — Plataforma Bancaria Core

Solución bancaria core full-stack para gestionar clientes, cuentas y movimientos. Construida con microservicios Spring Boot, gRPC, Kafka, PostgreSQL y Angular 13.

## Arquitectura

| Componente | Descripción | Puerto |
|-----------|-------------|------|
| `ms-core-client-person` | Dominio Cliente/Persona (gRPC) | 8081 / 9091 |
| `ms-core-account-movement` | Dominio Cuenta y Movimiento (gRPC + Kafka) | 8082 / 9093 |
| `ms-comp-remittances` | Gateway REST (WebFlux) | 8080 |
| `app-remittance` | SPA Angular (interfaz en español) | 4200 |
| PostgreSQL | 3 esquemas: `db-clients`, `db-accounts`, `db-movements` | 5432 |
| Kafka + Zookeeper | Eventos asíncronos de movimientos | 9092 / 2181 |

La comunicación interna usa **gRPC**. El composite expone **REST + JWT** al frontend.

### Decisiones de diseño

- Arquitectura hexagonal (puertos y adaptadores) en los microservicios core
- WebFlux solo en la capa composite
- Kafka para eventos de movimientos de alto volumen y aprovisionamiento asíncrono de cuentas
- Excepciones centralizadas en `commons-dependencies`
- UI basada en roles: los clientes ven solo sus datos; los administradores gestionan todos los registros pero no pueden realizar depósitos/retiros

## Requisitos previos

- JDK 17+, Maven 3.8+, Docker Desktop, Node 16+ (para desarrollo local del frontend)

## Inicio rápido (Docker)

```bash
chmod +x deploy.sh
./deploy.sh --skip-tests
```

| Servicio | URL |
|---------|-----|
| Frontend | http://localhost:4200 |
| API / Swagger | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

Las credenciales por defecto de la base de datos están en `.env.example` (copiar a `.env` antes del primer arranque).

### Credenciales de demostración (datos semilla)

| Rol | Identificación | Contraseña |
|------|----------------|----------|
| Admin | `1311322422` | `admin123` |
| Usuario (Jose Lema) | `1710034065` | `1234` |
| Usuario (Marianela Montalvo) | `0910000017` | `5678` |
| Usuario (Juan Osorio) | `1720000007` | `1245` |

### Plantillas de configuración

Los archivos sensibles están en `.gitignore`. Copiar las plantillas antes de ejecutar:

```bash
cp .env.example .env
cp docker-compose.yml.template docker-compose.yml
cp backend/composite/ms-comp-remittances/src/main/resources/application.properties.template \
   backend/composite/ms-comp-remittances/src/main/resources/application.properties
# repetir para otros archivos *.template
```

## Desarrollo local

```bash
# Backend
cd backend && mvn clean install

# Frontend
cd frontend/app-remittance && npm install && npm start
```

## Resumen de la API

| Recurso | Endpoints |
|----------|-----------|
| Auth | `POST /auth/login`, `POST /auth/register` |
| Clientes | `GET/POST/PUT/DELETE /clients`, `PATCH /clients/{id}/status` |
| Cuentas | `GET/POST/PUT/DELETE /accounts` |
| Movimientos | `GET/POST/PUT/DELETE /movements` |
| Reportes | `GET /reports?fecha={start,end}&cliente={id}` |

## Pruebas

```bash
cd backend && mvn clean test
```

### Postman

Importar [`postman_collection.json`](postman_collection.json) en Postman. Ejecutar primero **Auth → Login (User — Jose Lema)** (`1710034065` / `1234`); el script de prueba guarda el JWT en la variable de colección `token` para los endpoints protegidos. Todas las respuestas usan el sobre `ApiResponse` (`$.data` para datos, `$.data.token` en login).

Consultar [docs/TEST_COVERAGE.md](docs/TEST_COVERAGE.md) para las ubicaciones del informe JaCoCo.

### Pruebas de carga (JMeter)

**Requisitos previos:** stack en ejecución (`./deploy.sh --skip-tests`), API accesible en `:8080` y JMeter instalado en `tools/apache-jmeter-5.6.3/` (ver [tools/README.md](tools/README.md)).

| Modo | Comando | Notas |
|------|---------|-------|
| GUI | `./start-jmeter.sh` → **Start** | El plan `load_test.jmx` se carga automáticamente; el **Setup Thread Group** inicia sesión como `1710034065` / `1234` y obtiene el JWT |
| CLI + informe HTML (recomendado) | `./scripts/run-load-test.sh` | Abre `docs/jmeter/report/index.html` |

**Artefactos**

| Ruta | Descripción |
|------|-------------|
| `docs/jmeter/EVIDENCE.md`, `docs/jmeter/statistics.json` | Evidencia versionada |
| `docs/jmeter/report/` | Generado localmente (gitignored) |

**Solución de problemas:** si las peticiones muestran `Authorization: Bearer 1`, recargar `load_test.jmx` — la ruta del extractor JSON debe ser `$.data.token`. Confirmar que la API está activa en el puerto 8080 antes de ejecutar.

Guía completa: [docs/jmeter/LOAD_TEST.md](docs/jmeter/LOAD_TEST.md)

## Desarrollo asistido por IA

Carpeta de evidencia: `ai/`

| Archivo | Contenido |
|------|---------|
| `ai/prompts.md` | Resumen de prompts |
| `ai/decisions.md` | Salidas de IA aceptadas/rechazadas |
| `ai/generations/` | Fragmentos de código |
| `ai/agent/` | Skills de scaffolding |

## Checklist de entregables

- [x] `database/BaseDatos.sql`
- [x] Colección Postman (`postman_collection.json`)
- [x] Swagger UI + OpenAPI en `/v3/api-docs`
- [x] Frontend Angular
- [x] Dockerfiles + docker-compose
- [x] Pruebas unitarias e de integración + JaCoCo
- [x] Plan JMeter (`load_test.jmx`) + evidencia (`docs/jmeter/EVIDENCE.md`)
- [x] Carpeta de evidencia de IA

## Consideraciones no funcionales

- **Rendimiento**: llamadas internas gRPC, procesamiento asíncrono con Kafka para movimientos
- **Escalabilidad**: microservicios stateless, esquemas de BD independientes
- **Resiliencia**: health checks en docker-compose, mapeo graceful de errores gRPC
