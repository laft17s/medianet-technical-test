# Commons Dependencies

## Descripción
Este módulo es una librería Java (`jar`) compartida por todos los microservicios.

## Contenido
- **Interfaces y Proto Files (`.proto`)**: Define el contrato estricto de gRPC (mensajes y servicios) que usa la arquitectura. El código Java de los stubs de gRPC se auto-genera aquí mediante `protobuf-maven-plugin`.
- **Excepciones Globales**: Contiene las clases de excepción personalizadas y manejadores (`GlobalExceptionHandler` u orientados a gRPC).
- **DTOs compartidos**: Si existen DTOs comunes, se empaquetan aquí.
- **Configuraciones Comunes**: Configuraciones base como CORS, Seguridad (Filtros de WebFlux), JWT Utils, o constantes del sistema.

## Importancia
Evita la redundancia de código (DRY) y garantiza que tanto los clientes gRPC (composite) como los servidores gRPC (microservicios core) hablen exactamente el mismo idioma y compartan las mismas clases autogeneradas, eliminando fallos de deserialización.
