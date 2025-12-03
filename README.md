# Test Técnico - Juan José Pérez

Aplicación de microservicios para gestión de clientes y cuentas bancarias, con comunicación asíncrona mediante Kafka.

## ¿Qué necesitas?

- Docker y Docker Compose instalados
- Java 17 (solo si quieres correr los tests localmente)

## Cómo correrlo

### Primera vez (instalación inicial)

```bash
# 1. Construir y levantar todos los servicios
docker-compose up -d --build

# 2. Esperar para que todo inicie. Verificar que todos los servicios estén corriendo
docker-compose ps
```

Deberías ver 5 contenedores corriendo: postgres, zookeeper, kafka, customer-service y account-service.

**Nota importante:** 
- PostgreSQL crea automáticamente la base de datos `test_technique_juan_jose_perez` usando la variable `POSTGRES_DB` del `compose.yaml`
- Las tablas se crean automáticamente ejecutando el script `databases/create_db/2.create_tables.sql` la primera vez que se levanta el contenedor
- Si el volumen de PostgreSQL ya existe, los scripts de inicialización no se ejecutan (esto es normal)

### Si necesitas recrear todo desde cero

```bash
# Detener y eliminar contenedores, volúmenes y datos
docker-compose down -v

# Volver a levantar (creará todo de nuevo)
docker-compose up -d --build
```

### Si solo quieres reiniciar los servicios

```bash
# Detener servicios
docker-compose down

# Levantar de nuevo
docker-compose up -d
```

## URLs

- **Customer Service**: http://localhost:8081/api/v1
- **Account Service**: http://localhost:8082/api/v1
- **Swagger UI**: 
  - Customer: http://localhost:8081/api/v1/swagger-ui.html
  - Account: http://localhost:8082/api/v1/swagger-ui.html

## Especificaciones OpenAPI

Las especificaciones OpenAPI están disponibles en formato YAML en la carpeta `docs/`:

- `docs/customer-service-openapi.yaml` - Especificación del Customer Service
- `docs/account-service-openapi.yaml` - Especificación del Account Service

Para generar los archivos OpenAPI:

```bash
# Asegúrate de que los servicios estén corriendo
docker-compose up -d

# Ejecutar el script de generación
./generate-openapi.sh
```

Ver más detalles en [docs/README.md](docs/README.md)

## Probar que funciona

Hay un script que prueba todo el flujo de eventos de Kafka:

```bash
./docker-test-kafka.sh
```

Este script crea un cliente, verifica que el evento se publique y consuma, y luego intenta crear una cuenta. Si todo sale bien, los eventos de Kafka están funcionando correctamente.

## Comandos útiles

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker logs technique-customer-service --tail 50

# Detener todo
docker-compose down

# Detener y limpiar datos
docker-compose down -v
```

## Arquitectura

La aplicación está dividida en dos microservicios:

- **customer-service**: Gestiona clientes y personas. Publica eventos cuando se crean, actualizan o eliminan clientes.
- **account-service**: Gestiona cuentas, movimientos y reportes. Consume eventos de clientes para mantener una copia local (CustomerReference) y poder validar que existan antes de crear cuentas.

La comunicación entre servicios es asíncrona usando Kafka. Los eventos se publican en topics: `customer-created`, `customer-updated`, `customer-deleted`.

## Verificar que todo funciona

```bash
# Verificar health checks
curl http://localhost:8081/api/v1/actuator/health
curl http://localhost:8082/api/v1/actuator/health

# Verificar que las tablas se crearon
docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c "\dt"

# Deberías ver: cliente, cuenta, movimientos, customer_reference
```

## Si algo no funciona

1. **Revisa los logs:**
   ```bash
   docker-compose logs
   docker logs technique-customer-service --tail 50
   docker logs technique-account-service --tail 50
   ```

2. **Verifica que los puertos no estén ocupados:**
   ```bash
   lsof -i :8081  # Customer Service
   lsof -i :8082  # Account Service
   lsof -i :5433  # PostgreSQL (puerto externo)
   lsof -i :9092  # Kafka
   ```

3. **Si PostgreSQL no crea las tablas:**
   ```bash
   # Verificar que el contenedor esté corriendo
   docker-compose ps postgres
   
   # Verificar si las tablas existen
   docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c "\dt"
   
   # Si no existen, ejecutar el script manualmente
   docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -f /docker-entrypoint-initdb.d/2.create_tables.sql
   ```

4. **Reconstruir desde cero (elimina todos los datos):**
   ```bash
   docker-compose down -v
   docker-compose up -d --build
   ```
