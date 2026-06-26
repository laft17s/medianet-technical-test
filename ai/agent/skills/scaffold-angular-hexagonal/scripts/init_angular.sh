#!/bin/bash

# Generador de Proyecto Angular 13 - Arquitectura Hexagonal y pnpm
# Uso: bash init_angular.sh app-nombre

if [ -z "$1" ]; then
  echo "Error: Debes proporcionar el nombre de la aplicación."
  echo "Uso: bash init_angular.sh app-nombre"
  exit 1
fi

APP_NAME=$1

echo "Generando proyecto Angular 13 ($APP_NAME) forzando el uso de pnpm..."
# Ejecutamos el CLI de Angular a traves de pnpx
pnpx -y @angular/cli@13 new $APP_NAME --routing=true --style=css --packageManager=pnpm --strict=true

cd $APP_NAME

echo "Añadiendo Bootstrap y dependencias usando pnpm..."
pnpm add bootstrap@5.2.0

echo "Creando estructura de Arquitectura Hexagonal en frontend..."
# Core (Dominio, Casos de uso y Puertos)
mkdir -p src/app/core/domain
mkdir -p src/app/core/use-cases
mkdir -p src/app/core/ports

# Infrastructure (Adaptadores HTTP, Interceptors)
mkdir -p src/app/infrastructure/adapters
mkdir -p src/app/infrastructure/interceptors

# Presentation (Páginas y Componentes UI reutilizables)
mkdir -p src/app/presentation/pages
mkdir -p src/app/presentation/components

echo "Estructura generada exitosamente en el directorio: $APP_NAME"
tree src/app || find src/app -type d
