# AI Decisions Summary

Human validation of AI-generated output. Full history: `ai/decisions/decisions_history.md`.

## Accepted from AI

- Hexagonal architecture with ports/adapters in core microservices.
- gRPC for internal communication and WebFlux for the composite gateway.
- Kafka for async account creation after client registration.
- DTO mapping in REST controllers to avoid Jackson/protobuf serialization errors.

## Corrected manually

- Removed JMeter mock fallback in `MovementController` that returned fake 200 responses.
- Fixed `AccountDbAdapter` lookup for plain-text and encrypted account numbers in seed data.
- Moved admin role detection from hardcoded frontend constants to JWT claims from backend.
- Blocked administrators from creating deposits/withdrawals (business rule from requirements).

## Rejected

- Hardcoded API URLs and paths in Angular services (replaced with `environment.ts` and constants).
- Returning mock movement data on gRPC failures during load testing.

## Agent built

Location: `ai/agent/skills/`

- `scaffold-springboot-hexagonal` — initializes Maven hexagonal modules.
- `scaffold-angular-hexagonal` — initializes Angular feature modules.

Run instructions: see `ai/agent/README.md`.
