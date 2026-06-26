---
name: scaffold-angular-hexagonal
description: Automatiza la creación de un proyecto base Angular 13 con Arquitectura Hexagonal y pnpm para Medianet.
---

# Generador de Proyecto Angular (Arquitectura Hexagonal)

Cuando necesites inicializar la aplicación frontend del proyecto (`app-remittances`), debes invocar el script contenido en esta skill. Este script genera el esqueleto de Angular 13 configurado explícitamente para usar `pnpm` (cumpliendo con la regla global del proyecto) y crea las carpetas para aislar la lógica bajo los principios de Arquitectura Hexagonal.

## Uso

Para usar este andamiaje automático, el agente debe ejecutar el script bash `init_angular.sh` pasando como argumento el nombre del proyecto (ej: `app-remittances`).

**Comando** (desde la raíz del repositorio):

```bash
bash ai/agent/skills/scaffold-angular-hexagonal/scripts/init_angular.sh <nombre-app>
```

Al terminar, se instalarán dependencias clave (como Bootstrap y Angular Material si procede) a través de `pnpm` y se estructurará `src/app` con las carpetas Core, Infrastructure y Presentation.
