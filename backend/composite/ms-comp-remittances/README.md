# Composite Service - Remittances

## Descripción
Este microservicio es la **puerta de entrada (API Gateway / BFF)** de la arquitectura. Está desarrollado usando **Spring WebFlux (Reactivo)** como lo exige la definición del proyecto.

## Responsabilidades
- Recibir todo el tráfico REST proveniente del Frontend Angular.
- Traducir peticiones REST a llamadas **gRPC**.
- Consolidar respuestas (ej. juntar datos de la Cuenta con el nombre del Cliente llamando a ambos core services).
- Autenticación y validación de tokens JWT.

## Tecnologías
- Spring Boot 3+
- Spring WebFlux (Reactor)
- gRPC Client (para llamar a los ms-core)
- Springdoc OpenAPI (Swagger)
