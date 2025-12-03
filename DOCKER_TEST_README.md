# Guía de Pruebas en Docker

Esta guía explica cómo probar los eventos de Kafka directamente en el entorno Docker.

## Scripts Disponibles

### 1. `docker-test-kafka.sh`

Script completo que se ejecuta desde el host y prueba todo el flujo.

**Uso:**
```bash
chmod +x docker-test-kafka.sh
./docker-test-kafka.sh
```

**Qué hace:**
- Verifica que todos los servicios estén corriendo
- Verifica health checks
- Verifica que Kafka funcione
- Crea un cliente de prueba
- Espera a que el evento se procese
- Verifica logs de ambos servicios
- Verifica que CustomerReference se creó en la DB
- Intenta crear una cuenta para validar el flujo completo

### 2. `test-inside-docker.sh`

Script para ejecutar dentro de un contenedor Docker.

**Uso:**
```bash
# Opción 1: Copiar y ejecutar
docker cp test-inside-docker.sh technique-customer-service:/tmp/
docker exec -it technique-customer-service bash /tmp/test-inside-docker.sh

# Opción 2: Ejecutar directamente
docker exec -it technique-customer-service bash -c "cat > /tmp/test.sh << 'EOF'
$(cat test-inside-docker.sh)
EOF
bash /tmp/test.sh"
```

## Pruebas Manuales en Docker

### 1. Verificar que los servicios estén corriendo

```bash
docker-compose ps
```

Deberías ver:
- technique-postgres (Up)
- technique-zookeeper (Up)
- technique-kafka (Up)
- technique-customer-service (Up)
- technique-account-service (Up)

### 2. Crear un cliente desde el host

```bash
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Docker",
    "gender": "MALE",
    "identification": "1234567890",
    "address": "Test",
    "phone": "+593999999999",
    "password": "test123",
    "status": "ACTIVE"
  }'
```

### 3. Ver logs en tiempo real

```bash
# Terminal 1: Customer Service
docker logs -f technique-customer-service | grep -i "publish\|event"

# Terminal 2: Account Service
docker logs -f technique-account-service | grep -i "customer\|event\|reference"

# Terminal 3: Kafka
docker logs -f technique-kafka
```

### 4. Verificar en la base de datos

```bash
# Ver CustomerReference
docker exec -it technique-postgres psql -U myuser -d test_technique_juan_jose_perez \
  -c "SELECT * FROM CUSTOMER_REFERENCE;"

# Ver clientes
docker exec -it technique-postgres psql -U myuser -d test_technique_juan_jose_perez \
  -c "SELECT ID_CLIENTE, NOMBRE, IDENTIFICACION, ESTADO FROM CLIENTE;"
```

### 5. Probar eventos de Kafka directamente

```bash
# Entrar al contenedor de Kafka
docker exec -it technique-kafka bash

# Listar topics
kafka-topics --bootstrap-server localhost:9092 --list

# Consumir eventos del topic customer-created
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic customer-created \
  --from-beginning \
  --property print.key=true \
  --property print.value=true
```

En otra terminal, crea un cliente y verás el evento en tiempo real.

## Ejecutar Pruebas desde Dentro de los Contenedores

### Desde Customer Service

```bash
# Entrar al contenedor
docker exec -it technique-customer-service bash

# Crear cliente
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test",
    "gender": "MALE",
    "identification": "123",
    "address": "Test",
    "phone": "+593999",
    "password": "test",
    "status": "ACTIVE"
  }'

# Ver logs
tail -f /proc/1/fd/1 | grep -i "publish\|event"
```

### Desde Account Service

```bash
# Entrar al contenedor
docker exec -it technique-account-service bash

# Ver logs
tail -f /proc/1/fd/1 | grep -i "customer\|event\|reference"

# Crear cuenta (después de crear un cliente)
curl -X POST http://localhost:8082/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "number": "123456789",
    "accountType": "AHORROS",
    "initialBalance": 1000.00,
    "status": "ACTIVE",
    "customerId": 1
  }'
```

## Verificar Conectividad entre Servicios

```bash
# Desde Customer Service
docker exec technique-customer-service curl -s http://account-service:8082/actuator/health

# Desde Account Service
docker exec technique-account-service curl -s http://customer-service:8081/actuator/health

# Verificar Kafka desde cualquier servicio
docker exec technique-customer-service ping -c 2 kafka
docker exec technique-account-service ping -c 2 kafka
```

## Debugging

### Ver todos los logs juntos

```bash
docker-compose logs -f
```

### Ver logs de un servicio específico

```bash
# Últimas 100 líneas
docker logs technique-customer-service --tail 100

# Seguir logs en tiempo real
docker logs -f technique-account-service

# Buscar errores
docker logs technique-account-service 2>&1 | grep -i error
```

### Reiniciar un servicio

```bash
docker-compose restart customer-service
docker-compose restart account-service
```

### Ver variables de entorno

```bash
docker exec technique-customer-service env | grep KAFKA
docker exec technique-account-service env | grep KAFKA
```

## Verificar Topics de Kafka

```bash
# Listar topics
docker exec technique-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

# Ver detalles de un topic
docker exec technique-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic customer-created

# Ver mensajes en un topic
docker exec technique-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic customer-created \
  --from-beginning \
  --max-messages 10
```

## Limpiar y Reiniciar

```bash
# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (limpia datos)
docker-compose down -v

# Reiniciar
docker-compose up -d

# Ver logs mientras inician
docker-compose up
```

## Ejemplo de Flujo Completo

```bash
# 1. Levantar servicios
docker-compose up -d

# 2. Esperar a que inicien
sleep 15

# 3. Ejecutar script de prueba
./docker-test-kafka.sh

# 4. Si todo funciona, verás:
# ✓ Cliente creado
# ✓ Evento publicado
# ✓ Evento consumido
# ✓ CustomerReference creado
# ✓ Cuenta creada exitosamente
```

## Troubleshooting

### Los servicios no se comunican

```bash
# Verificar red Docker
docker network ls
docker network inspect technique_default

# Verificar que los servicios estén en la misma red
docker inspect technique-customer-service | grep NetworkMode
docker inspect technique-account-service | grep NetworkMode
```

### Kafka no está accesible

```bash
# Verificar que Kafka esté corriendo
docker logs technique-kafka

# Verificar conectividad desde los servicios
docker exec technique-customer-service ping -c 2 kafka
docker exec technique-account-service ping -c 2 kafka

# Verificar puertos
docker port technique-kafka
```

### Los eventos no se consumen

1. Verifica los logs de Account Service:
   ```bash
   docker logs technique-account-service | grep -i "kafka\|error\|exception"
   ```

2. Verifica la configuración de Kafka:
   ```bash
   docker exec technique-account-service env | grep KAFKA
   ```

3. Verifica que el topic exista:
   ```bash
   docker exec technique-kafka kafka-topics --bootstrap-server localhost:9092 --list
   ```

