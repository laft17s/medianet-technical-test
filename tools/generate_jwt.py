import sys

try:
    import jwt
except ImportError:
    print("Error: Necesitas instalar la libreria 'PyJWT'. Ejecuta el comando: pip install PyJWT")
    sys.exit(1)

import time

# La semilla (secret key) obtenida del codigo fuente del backend (JwtUtil.java)
SECRET_KEY = "SuperSecretKeyForJwtTokenGenerationMedianet123!"

# Puedes cambiar los valores si deseas generar un token para otro cliente especifico
payload = {
    "sub": "JMeter Load Test",
    "identification": "1710034065",
    "clientId": 1,
    "role": "user",
    "iat": int(time.time()),
    "exp": int(time.time()) + (3600 * 24 * 30) # Valid for 30 days in local testing
}

token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
print("\n" + "="*50)
print("🔑 SEMILLA / TOKEN JWT GENERADO EXITOSAMENTE 🔑")
print("="*50)
print("\nPuedes copiar y pegar el siguiente token en tu JMeter, Postman o Frontend:\n")
print(token)
print("\n" + "="*50 + "\n")
