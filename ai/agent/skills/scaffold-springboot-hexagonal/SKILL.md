---
name: scaffold-springboot-hexagonal
description: Automatiza la creación de un proyecto base Spring Boot 3 + Java 17 con Arquitectura Hexagonal para Medianet.
---

# Generador de Microservicios Spring Boot (Arquitectura Hexagonal)

Cuando necesites crear un nuevo microservicio backend para este proyecto, debes utilizar el script incluido en esta skill. Este script descarga un proyecto base desde Spring Initializr y crea la estructura estricta de carpetas para la Arquitectura Hexagonal.

## Uso

Para usar este andamiaje automático, el agente debe ejecutar el script bash `init_spring.sh` pasando como argumento el nombre del microservicio (en kebab-case, por ejemplo: `ms-core-client-person`).

**Comando** (desde la raíz del repositorio):

```bash
bash ai/agent/skills/scaffold-springboot-hexagonal/scripts/init_spring.sh <nombre-microservicio>
```

Una vez generado, el agente podrá continuar implementando los puertos, adaptadores, y casos de uso en las carpetas correspondientes.
