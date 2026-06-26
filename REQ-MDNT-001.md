# Prueba técnica para Ingeniero de Desarrollo

El presente documento es una prueba técnica para evaluar las habilidades y conocimientos de los candidatos para el puesto de Ingeniero de Desarrollo.

## Introducción

La prueba debe ser elaborada con los siguientes frameworks:

- Backend: Spring Boot (Java)
- Frontend: Angular (Typescript)

La prueba está orientada a evaluar las siguientes habilidades:

- Habilidades técnicas
- Habilidades de resolución de problemas
- Habilidades de comunicación
- Habilidades de trabajo en equipo
- Habilidades de liderazgo
- Habilidades de autogestión
- Habilidades de automotivación

> **Importante:**
> El desarrollo y entregables deben estar en inglés.
> Se permite el desarrollo asistido por IAs, siempre y cuando se incluya la documentación de las fuentes utilizadas.

## Contexto y Objetivo

Una entidad financiera está modernizando su plataforma core. El objetivo es construir un sistema bancario compuesto por microservicios REST que gestione clientes, cuentas y movimientos, junto con una interfaz web para la operación y consulta. El proyecto busca evaluar la capacidad del candidato para diseñar, implementar, probar y desplegar una solución completa aplicando buenas prácticas de ingeniería de software.

Adicionalmente, y como eje diferenciador de esta prueba, se evaluará el uso de Inteligencia Artificial aplicada al desarrollo: la capacidad de incorporar asistentes y/o agentes de IA para acelerar el ciclo de desarrollo sin sacrificar calidad, así como la habilidad para validar críticamente lo que la IA produce.

**Esta prueba está dirigida a un perfil Ingeniero de desarrollo, por lo que se espera
autonomía técnica, decisiones de diseño justificadas y un entregable funcional, probado y desplegable.**


#### Qué se evalúa

- **Diseño y arquitectura:** Separación de responsabilidades, patrones (Repository, capas, hexagonal opcional) y modelado de dominio.
- **Calidad del código:** Legibilidad, manejo de excepciones, validaciones y cobertura de pruebas.
- **Dominio full-stack:** Backend en Java/Spring Boot y frontend en Angular consumiendo la API.
• **Pruebas:** Unitarias e integración, con reporte de cobertura.
• **Despliegue:** Contenerización con Docker y orquestación mínima con docker-compose.
• **Uso de IA aplicada al desarrollo:** Evidencias, agentes/asistentes y validación humana delo generado.

## Alcance por perfil

Esta prueba consolida un alcance objetivo para el perfil **Ingeniero de desarrollo**. El candidato debe completar la totalidad del alcance descrito a continuación.

| Perfil | Alcance esperado |
| :--- | :--- |
| **Ingeniero**<br>(objetivo) | Separar la solución en 2 microservicios: **(Cliente, Persona)** y **(Cuenta, Movimientos)**, con comunicación entre ellos. Cumplir F1, F2, F3, F4, F5 y F6.<br><br>Frontend **Angular** para operar la API. Despliegue con **Docker**. Evidencia de uso de IA en el desarrollo (Sección 6). |

> **Consideraciones no funcionales a contemplar**
> (no necesariamente implementadas en su totalidad, pero sí razonadas en el *README*): rendimiento, escalabilidad y resiliencia.

## Herramientas y tecnologías

El candidato debe utilizar el siguiente stack. Se valorará el dominio de las herramientas listadas.

### Backend

| Categoría | Tecnología |
| :--- | :--- |
| Lenguaje | .NET 6.0 (C#) y/o Java (Spring Boot 3+, Java 17+) según la(s) capa(s) implementada(s) |
| Framework principal | Spring Boot 3+ para los microservicios |
| Persistencia | JPA / Hibernate (Java) o Entity Framework Core (.NET) sobre base de datos relacional |
| Base de datos | Base de datos relacional (PostgreSQL, SQL Server o MySQL) |
| Patrones | Repository, capas de servicio, DTOs, manejo centralizado de excepciones |

### Frontend

| Categoría | Tecnología |
| :--- | :--- |
| Framework | Angular 13.0 |
| UI / Componentes | Angular Material 13.0 |
| Estilos | Bootstrap 5.2.0 |
| Consumo de API | Cliente HTTP de Angular contra los endpoints REST del backend |

### Herramientas de lectura y pruebas de API

| Herramienta | Uso esperado |
| :--- | :--- |
| Postman | Validación funcional de los endpoints. Entregar colección exportada (.json). |
| Swagger / OpenAPI | Documentación del contrato de la API y exploración de endpoints. |
| JMeter | Pruebas de carga/rendimiento sobre al menos un endpoint crítico (registro de movimientos). |

## Modelo de dominio

Se deben implementar las siguientes entidades. Cada una debe manejar su clave primaria y, donde se indique, claves únicas.

> **Importante:**
> Las clases y atributos deben estar en inglés. Por ejemplo la clase Persona debe llamarse Person; el atributo nombre debe llamarse name, mismo escenario con género que debe llamarse gender y así para los demás casos.

### Persona (clase base)

**Atributos:**

- nombre
- género
- edad
- identificación
- dirección
- teléfono


> **Importante:**
> Debe manejar su clave primaria (PK).

### Cliente

*Entidad que hereda de Persona.*

**Atributos:**

- clienteId
- contraseña
- estado


> **Importante:**
> Debe manejar su clave única (PK).

### Cuenta

**Atributos:**

- numeroCuenta (PK, unique)
- tipoCuenta
- saldoInicial
- estado


> **Importante:**
> Debe manejar su clave única (PK).

### Movimiento

**Atributos:**

- fecha
- tipoMovimiento
- valor
- saldo


> **Importante:**
> Debe manejar su clave única (PK) y su relación con **Cuenta**.


## Funcionalidades del API

> **Importante:**
> La API REST debe exponer los verbos HTTP correspondientes (GET, POST, PUT, PATCH, DELETE) sobre los recursos indicados.

### F1 — CRUDs

Generación de operaciones de crear, leer, actualizar y eliminar para las entidades Cliente, Cuenta y Movimientos.

**Endpoints a exponer:**

```
/clientes
/cuentas
/movimientos
```

> **Importante:**
> Los endpoints paths deben estar en inglés. Por ejemplo /clientes debe llamarse /clients; así para el resto de paths.

### F2 — Registro de movimientos

- Un movimiento puede tener valores positivos (depósito) o negativos (retiro).
- Al registrar un movimiento debe actualizarse el saldo disponible de la cuenta.
- Se debe llevar el registro histórico de las transacciones realizadas.

### F3 - Validación de saldo

- Al realizar un movimiento que exceda el saldo disponible, se debe alertar con el mensaje “Saldo no disponible”.
- Defina, según su criterio, la mejor manera de capturar y exponer el error (manejo centralizado de excepciones).

### F4 - Reportes (Estado de cuenta)

- Generar un reporte de “Estado de cuenta” filtrando por rango de fechas y cliente.
- Debe incluir: cuentas asociadas con sus respectivos saldos y el detalle de movimientos de las cuentas.
- Endpoint: /reportes?fecha={rango}&cliente={id}
- El servicio debe retornar la información en formato JSON.

*Ejemplo de la estructura esperada por registro:*

```
{
   "Fecha":"10/2/2022",
   "Cliente":"Marianela Montalvo",
   "Numero Cuenta":"225487",
   "Tipo":"Corriente",
   "Saldo Inicial":100,
   "Estado":true,
   "Movimiento":600,
   "Saldo Disponible":700
}
```

> **Importante:**
> Los atributos del json de respuesta deben estar en inglés.

### F5 - Pruebas unitarias

- Implementar al menos 1 prueba unitaria sobre la entidad de dominio Cliente.
- Como mínimo se requieren 2 pruebas unitarias de endpoints en total.

### F6 - Pruebas de integración

- Implementar al menos 1 prueba de integración (por ejemplo, registro de movimiento que valide saldo y persistencia).

### F7 - Despliegue en contenedores

- Toda solución (microservicios, base de datos y frontend) debe correr usando **docker-compose**.

## Inteligencia artificial aplicada al desarrollo

Este es un componente central y obligatorio de la prueba. No se evalúa solo el resultado, sino cómo el candidato integra IA en su flujo de trabajo para optimizar tiempos y la capacidad de validar críticamente lo que la IA genera.

### Reto: construir un agente o asistente de apoyo al desarrollo

El candidato debe diseñar e implementar (o configurar) al menos UNA de las siguientes opciones, y documentarla. El objetivo es demostrar pensamiento de automatización con IA, no solo el uso puntual de un chatbot.

1. **Agente / asistente de desarrollo:** un agente que ayude a optimizar tareas repetitivas del proyecto. Ejemplos válidos: generación de DTOs y mappers a partir del modelo, generación de pruebas unitarias a partir de una clase, generación de la colección Postman desde el contrato OpenAPI, o un asistente que revise el código contra una checklist de buenas prácticas.
2. **Agente funcional dentro de la solución:** por ejemplo, un endpoint asistido por IA que genere un resumen en lenguaje natural del estado de cuenta de un cliente a partir de los movimientos, o que clasifique movimientos.
3. **Pipeline asistido por IA:** scripts o configuración (por ejemplo en VS Code / línea de comandos) que usen un modelo para acelerar scaffolding, refactors o generación de documentación, con resultados reproducibles.

### Qué se debe entregar como evidencia de IA

Incluir una carpeta `ai/` en el repositorio con la siguiente estructura mínima:

| Archivo / carpeta | Contenido |
| :--- | :--- |
| `ai/prompts.md` | Prompts utilizados (resumen), organizados por objetivo: scaffolding, pruebas, documentación, agente, etc. |
| `ai/generations/` | Fragmentos relevantes generados por la IA (borradores, código, tests). |
| `ai/decisions.md` | Qué se aceptó, qué se corrigió manualmente y por qué (validación humana). Decisiones de diseño influenciadas por la IA. |
| `ai/agent/` | Código y/o configuración del agente o asistente construido, con instrucciones para ejecutarlo. |

### Criterios de evaluación de la IA

* **Pertinencia:** la IA se usó para resolver problemas reales del proyecto, no de forma decorativa.
* **Optimización:** se evidencia ahorro de tiempo o reducción de errores.
* **Criterio humano:** el candidato detecta y corrige errores o alucinaciones de la IA.
* **Reproducibilidad:** el agente/asistente puede ejecutarse y los prompts están documentados.

## Casos de Uso (Datos de Ejemplo)

### Creación de clientes

| Nombre | Dirección | Teléfono | Contraseña | Estado |
| :--- | :--- | :--- | :--- | :--- |
| Jose Lema | Otavalo sn y principal | 098254785 | 1234 | True |
| Marianela Montalvo | Amazonas y NNUU | 097548965 | 5678 | True |
| Juan Osorio | 13 junio y Equinoccial | 098874587 | 1245 | True |

### Creación de cuentas

| N° Cuenta | Tipo | Saldo Inicial | Estado | Cliente |
| :--- | :--- | :--- | :--- | :--- |
| 478758 | Ahorro | 2000 | True | Jose Lema |
| 225487 | Corriente | 100 | True | Marianela Montalvo |
| 495878 | Ahorros | 0 | True | Juan Osorio |
| 496825 | Ahorros | 540 | True | Marianela Montalvo |

### Nueva cuenta corriente para Jose Lema

| N° Cuenta | Tipo | Saldo Inicial | Estado | Cliente |
| :--- | :--- | :--- | :--- | :--- |
| 585545 | Corriente | 1000 | True | Jose Lema |

### Movimientos a realizar

| N° Cuenta | Tipo | Saldo Inicial | Movimiento |
| :--- | :--- | :--- | :--- |
| 478758 | Ahorro | 2000 | Retiro de 575 |
| 225487 | Corriente | 100 | Depósito de 600 |
| 495878 | Ahorros | 0 | Depósito de 150 |
| 496825 | Ahorros | 540 | Retiro de 540 |

### Reporte de movimientos por fecha y cliente

| Fecha | Cliente | N° Cuenta | Movimiento | Saldo Inicial | Saldo Disp. |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 10/2/2022 | Marianela Montalvo | 225487 | 600 | 100 | 700 |
| 8/2/2022 | Marianela Montalvo | 496825 | -540 | 540 | 0 |

## Entregables

1. URL del repositorio o repositorios Git públicos con el código fuente (backend, frontend y carpeta ai/).
2. Script de base de datos con el nombre BaseDatos.sql (esquema, entidades y datos de ejemplo).
3. Colección de Postman exportada (.json) para validar los endpoints.
4. Documentación Swagger / OpenAPI del contrato de la API.
5. Frontend Angular funcional consumiendo la API.
6. Dockerfile y docker-compose para levantar la solución completa.
7.-Reporte de pruebas y cobertura de código.
8.-Evidencia de prueba de carga con JMeter sobre el endpoint crítico.
9.-Carpeta ai/ con prompts, generaciones, decisiones y el agente/asistente construido.

### README (requerido y obligatorio)

El archivo README debe contener:

- Contexto y decisiones de diseño y arquitectura.
- Pasos para ejecutar la solución de forma local y con Docker.
- Descripción de los microservicios y su comunicación.
- Sección de uso de IA: prompts, resumen de respuestas, fragmentos generados, correcciones y descripción del agente/asistente.

## Criterios de Evaluación

La calificación pondera tanto la solución como la defensa técnica posterior.

| Criterio | Peso | Detalle |
| :--- | :---: | :--- |
| Arquitectura y diseño | 20% | Microservicios, patrones, separación de capas, modelo de dominio. |
| Funcionalidad (F1–F7) | 25% | Cumplimiento de los requerimientos funcionales. |
| Calidad y pruebas | 20% | Cobertura, manejo de excepciones, validaciones, código limpio. |
| IA aplicada al desarrollo | 20% | Agente/asistente, evidencias y validación humana (Sección 6). |
| Frontend y despliegue | 15% | Angular funcional, Docker y docker-compose. |
| Defensa técnica | 100% | Capacidad de explicar y justificar la solución. |

## Diseño de solución

Se adjunta la arquitectura de solución, misma que puede estar sujeta a cambios en torno a nuevas directrices del desarrollo:

| Versión | Diagrama | Observación |
| :--- | :--- | :--- |
| Diseño de Solución | [ARQ-REMITTANCES-001](./ARQ-REMITTANCES-001.png) | Arquitectura inicial |

## Componentes a implementar

| Componente | Descripción | Capa | Tecnología |
| :--- | :--- | :--- | :--- |
| commons-dependencies | Dependency shared entre todos los microservicios para la comunicación entre ellos. Debe contener los mensajes de error, exito, codigos de exito y error y todos los datos en comun entre los microservicios. Prohibido reverbar una funcionalidad. | Dependency | Spring Boot |
| client-repository | Repositorio de clientes. Debe conectarse a una base postgres llamada db-clients y ser adjunta como dependencia en el **microservicio ms-core-client-person**. | Repository | Spring Boot |
| account-repository | Repositorio de cuentas. Debe conectarse a una base postgres llamada db-accounts y ser adjunta como dependencia en el **microservicio ms-core-account-movement**. | Repository | Spring Boot |
| movement-repository | Repositorio de movimientos. Debe conectarse a una base postgres llamada db-movements y ser adjunta como dependencia en el **microservicio ms-core-account-movement**. | Repository | Spring Boot |
| ms-core-client-person | Microservicio para la gestión de clientes. Debe contener los endpoints necesarios para la gestión de clientes. | Core Service | Spring Boot |
| ms-core-account-movement | Microservicio para la gestión de cuentas y movimientos. Debe contener los endpoints necesarios para la gestión de cuentas y movimientos. | Core Service | Spring Boot |
| ms-comp-remittances | Microservicio para la gestión de la puerta de entrada. Debe contener los endpoints necesarios para la gestión de la puerta de entrada. | Composite Service | Spring Boot |
| app-remittances | Aplicación Angular para la gestión de clientes, cuentas y movimientos. Debe contener los endpoints necesarios para la gestión de clientes, cuentas y movimientos. | Frontend | Angular |

Se debe  levantar adicionalmente:

- Kafka connect para encolar los movimientos que se realicen en la cuenta del cliente.
- Kafka topics y sus respectivos schemas y sufijos.
- Postgres Database. Se deben crear 3 esquemas (db-clients, db-accounts y db-movemnts) con sus respectivas tablas. Se debe incluir una tabla de historial en cada esquema, una de auditoría, otra de eventos y una de errores.

> **Información técnica:**
>
> - Los microservicios y el composite se comunican entre si por gRPC. Sin embargo, solo el microservicio ms-core-account-movement debe publicar mensajes a kafka, pues al día se pueden generar miles o millones de transacciones, lo que ocasionaría estrés o fallos en el microservicio; por tanto se encolarán y despacharán de forma asíncrona.
>
> - Todos los componentes deben utilizar de manera obligatoria arquitectura hexagonal (Ports and Adapters).
>
> - El composite debe usar WebFlux de Spring Boot, no puede usar reactive programming en otras capas (ej. dominio, infraestructura).
>
> Los microservicios deben usar JPA / Hibernate de manera obligatoria.
>
> - Se debe usar maven para la gestión de dependencias.
>
> - Se debe crear el archivo application.propperties dentro de cada proyecto.
>
> - Todos los microservicios deben compartir una estrategia de comunicación (kafka, gRPC o Rest), esta debe ser implementada en un repositorio llamado commons-dependencies y debe ser inyectado en todos los microservicios. Asímismo, esta dependencia debe tener los mensajes de error, exito, codigos de exito y error, funciones para detallar los logs y todos los datos en comun entre los microservicios. Prohibido reverbar una funcionalidad.
>
> - Se deben aplicar patrones de diseño Builder, Factory, Strategy, Decorator y Constants (este último es importante para NO quemar ningún dato).
>
> El frontend debe tener los siguientes modulos:
>
> 1. Login y Register
> 2. Información de clientes
> 3. Información de cuentas
> 4. Información de movimientos
> 5. Realizar retiros y depósitos.
>
> - Si ingreso con usuario cliente, solo podrá ver su información y la de sus cuentas.
>
> - Si ingreso con usuario administrador, podrá ver la información de todos los clientes, cuentas y movimientos.
>
> - Si ingreso con usuario administrador, NO podrá realizar retiros y depósitos en cualquier cuenta, pero si puede gestionar tareas para realizar ajustes de retiros en caso de fallos o devoluciones.


## Indicaciones finales

- Aplique las buenas prácticas y patrones que considere necesarios; se evalúan explícitamente.
- El manejo de entidades debe realizarse mediante JPA / Hibernate o Entity Framework Core.
- Tiempo estimado de desarrollo: 4 días.
- Posterior a la entrega se agendará una entrevista técnica donde el candidato deberá defender la solución planteada.
- La entrega debe realizarse antes de la fecha y hora indicada por correo.