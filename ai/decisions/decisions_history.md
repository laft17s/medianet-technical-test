# Historial de Decisiones del Proyecto Medianet Technical Test

Este documento recopila de manera cronológica las decisiones Arquitectónicas, de Código y de Negocio adoptadas durante el desarrollo de la solución.

## 1. Decisiones Arquitectónicas (Backend y Frontend)
- **Arquitectura de Microservicios:** Se optó por una separación estricta mediante microservicios, identificando dos servicios *Core* (`ms-core-client-person` y `ms-core-account-movement`) y un servicio compuesto/Gateway (`ms-comp-remittances`) que orquesta a los demás.
- **Comunicación Síncrona gRPC:** Se implementó gRPC entre el gateway (`ms-comp-remittances`) y los microservicios *Core* para asegurar un rendimiento alto y baja latencia en el tráfico interno.
- **Comunicación Asíncrona con Apache Kafka:** Para desacoplar flujos pesados (ej. cuando se crea un cliente, se debe crear automáticamente una cuenta y un movimiento de depósito inicial), se adoptó una arquitectura orientada a eventos publicando en un tópico (`client-registered-topic`) al que `ms-core-account-movement` se suscribe.
- **Arquitectura Hexagonal (Puertos y Adaptadores):** Se utilizó esta arquitectura en los microservicios *Core* para asegurar la separación de preocupaciones (`domain`, `application`, `infrastructure`), manteniendo la lógica de negocio pura y completamente aislada de la persistencia o los frameworks.
- **Frontend SPA (Angular):** Se eligió Angular junto con Angular Material para una experiencia de usuario rápida y dinámica, estructurando el código en módulos (`auth`, `clientes`, `cuentas`, `movimientos`) y servicios inyectables.
- **Dockerización Completa:** Cada componente de la infraestructura (Bases de datos PostgreSQL, Kafka, Zookeeper) y cada microservicio Java fueron empaquetados usando Docker y centralizados en un único `docker-compose.yml`.

## 2. Decisiones de Código y Refactorización
- **Uso Estricto de `pnpm`:** Se determinó vía reglas de repositorio utilizar siempre `pnpm` en lugar de `npm` para gestionar las dependencias de Angular y procesos de Node.js.
- **Migración a UUIDs para Identificadores:** Para evitar exponer IDs secuenciales en los endpoints (lo cual facilita vulnerabilidades de enumeración tipo BOLA/IDOR), se reemplazó el uso de IDs numéricos simples en las transacciones por identificadores únicos y seguros (UUID).
- **Mapeo de Protobuf a DTOs (Data Transfer Objects):** Para evitar errores de serialización `Error 500` por parte de Jackson (Spring Boot) al intentar convertir clases propias de gRPC, se decidió mapear las respuestas de los clientes gRPC (`MovementResponse`, `ReportItem`) hacia DTOs planos de Java en los Controladores del Gateway antes de enviarlos al frontend vía REST.
- **Cálculo Real del Saldo (Balance):** Se resolvió un error de negocio que causaba que el balance siempre se mostrara como cero al crear cuentas. La corrección se enfocó en consultar e inyectar el último balance cruzado de movimientos dentro de la entidad cuenta (Account).
- **Enmascaramiento de Datos Sensibles:** Se acordó no mostrar los números de cuenta completos en los selectores/tablas del frontend, implementando métodos que ocultan el string con 'X' (ej. `FXXXXXXX8`).
- **Validaciones en Tiempo Real (Frontend):** Se incluyó en Angular validaciones robustas como `invalidEcuadorianId` (usando un validador propio basado en el módulo 10) y conversión automática de texto a MAYÚSCULAS mediante directivas sin depender del backend para bloquear caracteres inválidos (símbolos).
- **Delegación de Autorización al Backend (Roles):** Se eliminó la verificación insegura de credenciales hardcodeadas (ej. la constante `ADMIN_ID`) dentro del código fuente de Angular. La lógica de rol se trasladó hacia el login de la API, la cual ahora expide dinámicamente un rol basado en configuraciones inyectadas en las variables de entorno de Spring Boot.

## 3. Decisiones de Negocio y Producto
- **Flujo Especial del Administrador:** Se acordó que el rol "Admin" debe tener acceso completo a la visualización de los Clientes, así como poder buscar y filtrar los reportes de movimientos de cualquier cliente (sin la necesidad de enviar su propio `clientId`).
- **Restricción Exclusiva del Admin (Identificación):** Se decidió asignar al Administrador del sistema la cédula quemada `1311322421` para permitir sus ingresos bajo permisos privilegiados.
- **Depósito Inicial Obligatorio:** Se implementó una regla core: cada vez que un cliente es registrado en el sistema, de forma automática por debajo se le abre su primera cuenta de "Ahorros" y se abona un movimiento por concepto de "DEPÓSITO INICIAL" con valor de `$5.00`.
- **Generación de Reportes PDF Dinámicos:** En la vista de Movimientos, los usuarios pueden filtrar reportes basados en fechas. Se decidió la generación local de PDF en el navegador vía `jsPDF` y `jspdf-autotable` para optimizar rendimiento de red, consultando al servidor únicamente los datos del período requerido.
- **Diseño Moderno y Tema Oscuro:** A nivel UI/UX se optó por abandonar interfaces genéricas y aplicar colores corporativos predefinidos (`#ffb703`, `#023047`), aplicando responsividad extrema para móviles.
