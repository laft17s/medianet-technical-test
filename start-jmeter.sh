#!/bin/bash

# Levanta la GUI de Apache JMeter con el plan de pruebas del proyecto.
# El token JWT se obtiene automáticamente en el Setup Thread Group (POST /auth/login).
# Requisito: la API debe estar corriendo en http://localhost:8080

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JMETER="$SCRIPT_DIR/tools/apache-jmeter-5.6.3/bin/jmeter"
JMX="$SCRIPT_DIR/load_test.jmx"

if [ ! -x "$JMETER" ]; then
  echo "JMeter no encontrado en $JMETER"
  echo "Consulte tools/README.md para instalarlo."
  exit 1
fi

echo "Comprobando API en http://localhost:8080 ..."
if ! curl -sf "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
  echo ""
  echo "ADVERTENCIA: la API no responde. Inicie el stack antes de ejecutar la prueba:"
  echo "  ./deploy.sh --skip-tests"
  echo ""
fi

echo "Iniciando Apache JMeter..."
echo "Plan de pruebas: load_test.jmx"
echo "Autenticación: Setup Thread Group obtiene JWT automáticamente (usuario 1710034065)."
echo ""

"$JMETER" -t "$JMX"
