---
name: client-automation
description: Skill para automatizar la creación masiva de clientes y cuentas con saldos iniciales (depósitos).
---

# Client & Account Automation

Esta skill instruye al agente sobre cómo crear un script o flujo automatizado que registre clientes de prueba y les asigne cuentas con fondos iniciales para validar los requerimientos F1, F2 y casos de uso.

## Flujo de Automatización
Cuando se invoque la automatización de clientes, el script debe:
1. Llamar al endpoint `POST /clients` (o vía gRPC) para registrar el cliente con nombre, identificación, dirección, etc.
2. Llamar al endpoint `POST /accounts` asociando el `clienteId` generado, con su respectivo tipo de cuenta (Ahorro/Corriente).
3. Si la cuenta requiere un saldo inicial, se debe registrar como un **Movimiento de tipo Depósito** llamando a `POST /movements`.

## Ejemplo de Script Shell / cURL
En la carpeta `scripts` puedes generar un archivo `create_demo_data.sh` que lea de un CSV o defina un array estático en bash, interando sobre cada registro y ejecutando los cURL correspondientes contra el API Gateway / Composite (`ms-comp-remittances`).

Ejemplo de invocación de la API:
```bash
# Crear Cliente
CLIENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/clients -H "Content-Type: application/json" -d '{"name": "Jose Lema", "gender": "Male", "age": 30, "identification": "17xxxx", "address": "Otavalo", "phone": "098254785", "password": "1234", "status": true}')

# Extraer ID
CLIENT_ID=$(echo $CLIENT_RESPONSE | jq -r '.id')

# Crear Cuenta
ACCOUNT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/accounts -H "Content-Type: application/json" -d '{"accountNumber": "478758", "type": "Ahorro", "status": true, "clientId": "'$CLIENT_ID'"}')

# Realizar el depósito (Saldo Inicial)
curl -s -X POST http://localhost:8080/api/movements -H "Content-Type: application/json" -d '{"accountNumber": "478758", "type": "Deposit", "value": 2000}'
```
