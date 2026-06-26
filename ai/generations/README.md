# AI Generations

Sample fragments generated or assisted by AI during the project.

## Movement REST DTO mapping

Generated pattern to avoid exposing gRPC types directly:

```java
public static MovementRestResponse from(MovementResponse mov) {
    MovementRestResponse dto = new MovementRestResponse();
    dto.movementId = mov.getMovementId();
    dto.movementType = mov.getMovementType();
    dto.value = mov.getValue();
    dto.balance = mov.getBalance();
    return dto;
}
```

## Global exception handler

AI draft adapted for WebFlux:

```java
@ExceptionHandler(InsufficientBalanceException.class)
public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
}
```

## Integration test scaffold

AI-generated starting point for `MovementIntegrationTest` using H2 and mocked Kafka.

See also: `ai/generations/.gitkeep`
