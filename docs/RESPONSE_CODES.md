# Catálogo de códigos de respuesta API

Todas las respuestas de negocio del composite (`ms-comp-remittances`) usan **HTTP 200** con un cuerpo JSON estandarizado. El frontend interpreta el campo `type` para mostrar popups de **acierto**, **fallo** o **alerta** — no se usan códigos HTTP (400/500) para comunicar resultados de negocio.

## Formato

```json
{
  "code": "OK-000",
  "type": "success",
  "message": "Operación exitosa",
  "data": { }
}
```

| Campo     | Descripción                                      |
|-----------|--------------------------------------------------|
| `code`    | Código del catálogo (ej. `ERR-001`)              |
| `type`    | `success` \| `error` \| `warning`                |
| `message` | Mensaje legible para el usuario                  |
| `data`    | Payload opcional (solo en respuestas exitosas)   |

## Catálogo

| Código   | Tipo      | Mensaje por defecto                         | Uso |
|----------|-----------|---------------------------------------------|-----|
| `OK-000` | success   | Operación exitosa                           | Respuesta exitosa genérica |
| `ERR-001`| error     | Saldo no disponible                         | Retiro con saldo insuficiente |
| `ERR-002`| error     | Recurso no encontrado                       | Entidad no existe |
| `ERR-003`| error     | Operación no permitida                      | Admin bloqueado en depósitos/retiros |
| `ERR-004`| error     | Argumento inválido                          | Validación / parámetros incorrectos |
| `ERR-005`| error     | Violación de regla de negocio               | Reglas de dominio |
| `ERR-006`| error     | Credenciales inválidas o usuario inactivo     | Login fallido |
| `ERR-999`| error     | Error interno del servidor                  | Error no controlado (sin stack trace) |
| `WRN-001`| warning   | Advertencia                                 | Casos no bloqueantes |

## Ejemplos

### Éxito — crear movimiento

```json
{
  "code": "OK-000",
  "type": "success",
  "message": "Operación exitosa",
  "data": {
    "movementId": 1,
    "movementType": "DEPOSITO",
    "value": 10.0,
    "balance": 2010.0
  }
}
```

### Fallo — saldo insuficiente

```json
{
  "code": "ERR-001",
  "type": "error",
  "message": "Saldo no disponible"
}
```

### Fallo — admin no puede operar

```json
{
  "code": "ERR-003",
  "type": "error",
  "message": "Administrators cannot register deposits or withdrawals"
}
```

## Implementación

- **Backend:** `commons-dependencies` → `ResponseCode`, `ResponseType`, `ApiResponse`
- **Manejo de errores:** `GlobalExceptionHandler` (REST) y `GrpcExceptionAdvice` (gRPC interno)
- **Frontend:** `ApiResponseInterceptor` desempaqueta `data` y muestra snackbars según `type`
