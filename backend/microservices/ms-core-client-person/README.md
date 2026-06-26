# Microservicio - Core Client Person

## Descripción
Este microservicio es el corazón de la gestión de la entidad `Persona` y `Cliente`. Pertenece a la capa de servicios Core y su propósito es exponer operaciones a través de gRPC, abstrayendo completamente la lógica de negocio y persistencia de las capas superiores.

## Arquitectura Hexagonal
Implementado bajo los principios de Arquitectura Hexagonal (Ports & Adapters):
- **Domain (`/domain`)**: Contiene las clases de dominio puro (`ClientDomain`, `PersonDomain`), sin dependencias a frameworks.
- **Application (`/application`)**: Implementa los casos de uso (`ClientService`) y define los puertos (`ClientRepositoryPort`, `ClientGrpcPort`).
- **Infrastructure (`/infrastructure`)**: 
  - Adapters de Entrada: Controladores gRPC que escuchan peticiones.
  - Adapters de Salida: Comunicación con `client-repository` (Postgres).

## Base de Datos
- Escribe exclusivamente en el esquema `db-clients`.
- Mantiene dependencias hacia el artefacto maven `client-repository`.
