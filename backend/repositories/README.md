# Repositories

## Descripción
Esta carpeta contiene los módulos Maven orientados exclusivamente al acceso a datos:
- `client-repository`
- `account-repository`
- `movement-repository`

## ¿Por qué separarlos?
Para cumplir estrictamente con el principio de Arquitectura Hexagonal y la escalabilidad:
1. **Desacoplamiento**: Los repositorios son adaptadores secundarios (`Output Adapters`). La lógica de dominio desconoce si usamos Postgres, SQL Server o Mongo.
2. **Reusabilidad**: Si en el futuro otro microservicio necesita consultar a nivel bajo estas entidades, simplemente incluye la dependencia Maven en lugar de duplicar código.
3. **Mapeo Limpio**: Aquí residen las entidades JPA (Entities) y los `Mappers` (MapStruct/Manual) para convertirlos a Objetos de Dominio puros.

Los repositorios se inyectan en los Microservicios Core mediante sus correspondientes `Ports`.
