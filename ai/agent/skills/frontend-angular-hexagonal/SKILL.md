---
name: frontend-angular-hexagonal
description: Guidelines for developing the frontend using Angular 13 and Hexagonal Architecture.
---

# Angular 13 - Hexagonal Architecture

Cuando trabajes en el frontend (`app-remittances`), DEBES aplicar los siguientes lineamientos:

## 1. Stack Tecnológico
- **Framework:** Angular 13.0
- **UI:** Angular Material 13.0
- **Estilos CSS:** Bootstrap 5.2.0

## 2. Arquitectura Hexagonal en Frontend
Para desacoplar la lógica de negocio de la UI y los detalles de infraestructura (HTTP requests), divide la estructura en capas:

```
src/app/
├── core/
│   ├── domain/ (Modelos de datos, interfaces y lógica pura de negocio en TypeScript)
│   ├── use-cases/ (Lógica de orquestación, llama a los puertos)
│   └── ports/ (Interfaces para repositorios o servicios externos)
├── infrastructure/ (Implementación de los puertos)
│   ├── adapters/ (Llamadas HTTP a la API REST)
│   └── interceptors/ (Manejo de tokens, logging de requests)
└── presentation/ (Componentes UI)
    ├── pages/ (Componentes enrutados)
    └── components/ (Componentes reutilizables de UI, Material Design)
```

## 3. Reglas
- **Módulos Requeridos:** Login/Register, Clients Info, Accounts Info, Movements Info, Realizar retiros/depósitos.
- **Inyección de Dependencias:** Usa los proveedores de Angular para inyectar los adaptadores de infraestructura donde se requieran los puertos definidos en el core.
- **Idioma:** Nombres de componentes, servicios, rutas y variables en **Inglés**.
- **Gestión de Estado:** Usa RxJS (BehaviorSubjects) o Ngrx (si la complejidad lo amerita) para mantener el estado sincronizado entre vistas de forma reactiva, evitando re-peticiones innecesarias.
