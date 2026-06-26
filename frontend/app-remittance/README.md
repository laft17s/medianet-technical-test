# Frontend - App Remittances

## Descripción
Este es el portal web transaccional desarrollado en **Angular 13**. Su propósito es ofrecer una interfaz gráfica amigable, moderna (estilo Glassmorphism) y responsiva para operar la plataforma de core bancario.

## Funcionalidades Principales
- **Autenticación (Login/Registro)**: Módulo de acceso seguro.
- **Roles (Admin vs Cliente)**: Los clientes ven su información particular. Los administradores tienen acceso global.
- **Gestión de Clientes y Cuentas**: Creación, actualización y visualización.
- **Movimientos**: Panel para registrar retiros o depósitos y consultar historiales.
- **Diseño Moderno**: Uso exhaustivo de variables CSS y Angular Material para un look & feel premium, adaptativo para escritorio y teléfonos móviles.

## Comandos de Desarrollo
- `npm run start` para correr la aplicación localmente (conecta al API local).
- `npm run build` para construir la aplicación para producción.

## Estructura
- `/src/app/core`: Guardias, interceptores HTTP, y servicios globales (Autenticación).
- `/src/app/modules`: Módulos de funcionalidad (Clientes, Cuentas, Movimientos) estructurados en un enfoque feature-module.
