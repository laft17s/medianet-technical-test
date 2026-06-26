# AI Prompts Summary

This file summarizes the main prompts used during development. The full chronological history is available in `ai/prompts/prompts_history.md`.

## Scaffolding

- Generate hexagonal Spring Boot microservice structure for client, account and movement domains.
- Create Angular modules for auth, clients, accounts and movements with Material UI.

## Backend

- Fix gRPC serialization issues by mapping protobuf responses to REST DTOs.
- Implement JWT authentication and role-based access in the composite API.
- Fix account balance calculation and encrypted account number lookup.

## Testing

- Create JUnit integration test for movement registration with balance validation.
- Generate JMeter load test plan for `POST /movements`.

## DevOps

- Create unified `deploy.sh` and `docker-compose.yml` for the full stack.
- Add `.gitignore` and configuration templates to avoid committing secrets.

## Agent

- Build Cursor skills for scaffolding Spring Boot hexagonal projects and Angular apps.
