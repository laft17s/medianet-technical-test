# Test Coverage Report

## How to generate

From the project root:

```bash
cd backend
mvn clean test
```

JaCoCo reports are generated per module under:

- `backend/microservices/ms-core-client-person/target/site/jacoco/index.html`
- `backend/microservices/ms-core-account-movement/target/site/jacoco/index.html`
- `backend/composite/ms-comp-remittances/target/site/jacoco/index.html`

## Test suites included

| Suite | Type | Module |
|-------|------|--------|
| `ClientDomainTest` | Unit (domain) | ms-core-client-person |
| `ClientServiceTest` | Unit (service) | ms-core-client-person |
| `AccountServiceTest` | Unit (service) | ms-core-account-movement |
| `MovementServiceTest` | Unit (service) | ms-core-account-movement |
| `MovementIntegrationTest` | Integration | ms-core-account-movement |
| `MovementControllerTest` | Unit (endpoint) | ms-comp-remittances |
| `ClientControllerTest` | Unit (endpoint) | ms-comp-remittances |

## Notes

- Integration tests use H2 in-memory databases (`application-test.properties`).
- Composite endpoint tests mock gRPC stubs and disable reactive security for isolated controller testing.
