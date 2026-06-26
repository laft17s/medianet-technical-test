#!/bin/bash

# Generador de Microservicio Spring Boot 3 + Java 17 - Arquitectura Hexagonal
# Uso: bash init_spring.sh ms-nombre-microservicio

if [ -z "$1" ]; then
  echo "Error: Debes proporcionar el nombre del microservicio."
  echo "Uso: bash init_spring.sh ms-nombre-microservicio"
  exit 1
fi

APP_NAME=$1
PACKAGE_NAME="com.medianet.${APP_NAME//-/_}"
PACKAGE_PATH="com/medianet/${APP_NAME//-/_}"

echo "Generando proyecto base $APP_NAME desde Spring Initializr..."
curl -s https://start.spring.io/starter.zip \
  -d dependencies=web,webflux,data-jpa,postgresql,validation,lombok \
  -d javaVersion=17 \
  -d bootVersion=3.4.0 \
  -d type=maven-project \
  -d groupId=com.medianet \
  -d artifactId=$APP_NAME \
  -d name=$APP_NAME \
  -o $APP_NAME.zip

echo "Descomprimiendo proyecto..."
unzip -q $APP_NAME.zip -d $APP_NAME
rm $APP_NAME.zip

echo "Creando estructura de Arquitectura Hexagonal..."
cd $APP_NAME

# Carpetas de aplicación (Casos de uso y puertos)
mkdir -p src/main/java/$PACKAGE_PATH/application/port/in
mkdir -p src/main/java/$PACKAGE_PATH/application/port/out
mkdir -p src/main/java/$PACKAGE_PATH/application/service

# Carpetas de dominio (Modelos y Excepciones Puras)
mkdir -p src/main/java/$PACKAGE_PATH/domain/model
mkdir -p src/main/java/$PACKAGE_PATH/domain/exception

# Carpetas de infraestructura (Adaptadores, Entidades JPA y Mappers)
mkdir -p src/main/java/$PACKAGE_PATH/infrastructure/adapter/in
mkdir -p src/main/java/$PACKAGE_PATH/infrastructure/adapter/out
mkdir -p src/main/java/$PACKAGE_PATH/infrastructure/entity
mkdir -p src/main/java/$PACKAGE_PATH/infrastructure/mapper

echo "Estructura generada exitosamente en el directorio: $APP_NAME"
tree src/main/java/$PACKAGE_PATH || find src/main/java/$PACKAGE_PATH -type d
