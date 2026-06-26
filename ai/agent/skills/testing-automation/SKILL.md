---
name: testing-automation
description: Guidelines and requirements for automating unit and integration testing.
---

# Testing Automation (JUnit, Mockito, TestContainers)

Para la implementación de pruebas, asegúrate de aplicar estos lineamientos:

## 1. Pruebas Unitarias
- Utiliza **JUnit 5** y **Mockito**.
- Evalúa la capa de **Dominio** de forma aislada, sin levantar el contexto de Spring (`@SpringBootTest` PROHIBIDO en pruebas unitarias).
- Crea un mínimo de **1 prueba unitaria** sobre la entidad de dominio `Client`.
- Crea un mínimo de **2 pruebas unitarias** probando los controladores REST (usando `@WebMvcTest`).

## 2. Pruebas de Integración
- Utiliza **TestContainers** para levantar una base de datos PostgreSQL real durante los tests, garantizando una validación fidedigna de persistencia.
- Implementa al menos **1 prueba de integración** para el flujo de Movimientos (verificando que rechace movimientos cuando no hay saldo y verifique persistencia en DB).
- Se requiere `@SpringBootTest` para integración de principio a fin, excluyendo dependencias externas (usa WireMock para mockear llamadas gRPC o HTTP a otros microservicios si es necesario).

## 3. Cobertura de Código
- Configura **Jacoco** en el `pom.xml` para generar un reporte automático de cobertura en cada `mvn test` o `mvn verify`.
- Las aserciones deben usar una librería expresiva como **AssertJ** (`assertThat(...)`).
