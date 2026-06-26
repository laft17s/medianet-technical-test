# Microservicio - Core Account Movement

## Descripción
Este microservicio core es el responsable de gestionar las entidades `Cuenta` y `Movimiento`. Contiene la lógica de negocio más crítica: la validación de saldos y la transacción financiera propiamente dicha.

## Características de Diseño
- **Arquitectura Hexagonal**: Separación estricta entre dominio, aplicación e infraestructura.
- **Transaccionalidad**: Valida que el `saldoDisponible` sea suficiente al crear movimientos. Si hay saldo insuficiente, lanza una excepción de dominio.
- **Asincronismo**: Implementa un Adapter de Salida (Output Adapter) hacia **Kafka**. Por cada movimiento exitoso, se publica un mensaje asíncrono en un tópico de Kafka para cumplir con el requerimiento de alta concurrencia sin estresar la base de datos principal.

## Base de Datos
- Opera sobre los repositorios `account-repository` y `movement-repository`.
- Los datos se dividen internamente en los esquemas `db-accounts` y `db-movemnts`.
