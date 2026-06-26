---
name: backend-spring-hexagonal
description: Guidelines and best practices for developing the backend microservices using Spring Boot 3, Java 17, and Hexagonal Architecture.
---

# Spring Boot 3 & Java 17 - Hexagonal Architecture

Cuando actúes sobre el backend del proyecto Medianet, DEBES seguir estrictamente estas reglas:

## 1. Stack Tecnológico
- **Lenguaje:** Java 17 (Usa records, switch expressions, text blocks, etc.)
- **Framework:** Spring Boot 3+ (Jakarta EE 10, Spring 6)
- **Persistencia:** JPA / Hibernate (usando el namespace `jakarta.persistence.*`)
- **Gestor de Dependencias:** Maven.
- **Base de Datos:** PostgreSQL (esquemas separados: `db-clients`, `db-accounts`, `db-movements`).

## 2. Arquitectura Hexagonal (Ports and Adapters)
Todo microservicio debe respetar la siguiente estructura de paquetes:

```
com.medianet.<domain>
├── application (Casos de uso)
│   ├── port
│   │   ├── in (Interfaces que la capa de infraestructura usa para hablar con el dominio)
│   │   └── out (Interfaces que el dominio usa para hablar con infraestructura, ej. repositorios)
│   └── service (Implementación de los puertos de entrada)
├── domain (Core del negocio)
│   ├── model (Entidades de dominio, sin anotaciones de JPA)
│   └── exception (Excepciones específicas del dominio)
└── infrastructure (Adaptadores)
    ├── adapter
    │   ├── in (Web/REST, gRPC, listeners de Kafka)
    │   └── out (Persistencia JPA, clientes HTTP/gRPC, producers de Kafka)
    ├── entity (Entidades de JPA/Hibernate)
    └── mapper (Mapeadores entre Entity <-> Model <-> DTO)
```

## 3. Reglas de Diseño
- **Independencia del Dominio:** La capa de `domain` y `application` NO deben tener dependencias de Spring Boot (salvo inyección de dependencias estandarizada vía `@Named` o configuración de beans) ni de JPA.
- **Mapeo:** Utiliza MapStruct o constructores/fábricas manuales para convertir entre Entidades JPA, Entidades de Dominio y DTOs REST.
- **Patrones Obligatorios:**
  - **Builder:** Para entidades complejas.
  - **Factory:** Para la creación de instancias complejas en dominio.
  - **Strategy:** Para lógicas de cálculo (ej. cálculos de saldos).
  - **Constants:** Evitar variables "quemadas" (hardcoded).
- **Comunicación:**
  - El microservicio `ms-core-account-movement` publica eventos asíncronos en Kafka.
  - La comunicación entre microservicios (ej. `ms-core-client-person` y `ms-core-account-movement`) vía gRPC.

## 4. Idioma
- Clases, métodos, atributos, endpoints y variables DEBEN estar en **Inglés**. (ej. `Client`, `Account`, `Movement`, `/clients`).
