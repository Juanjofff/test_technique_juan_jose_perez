# Guía para Validar Eventos de Kafka

## 1. Levantar la Infraestructura

Primero, asegúrate de que todos los servicios estén corriendo:

```bash
docker-compose up -d
```

Verifica que todos los servicios estén saludables:

```bash
# Verificar estado de los servicios
docker-compose ps

# Verificar health checks
curl http://localhost:8081/actuator/health  # Customer Service
curl http://localhost:8082/actuator/health  # Account Service
```

## 2. Verificar que Kafka está Funcionando

### Ver logs de Kafka

```bash
docker logs technique-kafka
```

Deberías ver mensajes como:
```
[KafkaServer id=1] started (kafka.server.KafkaServer)
```

### Verificar que los topics se crearon

```bash
# Entrar al contenedor de Kafka
docker exec -it technique-kafka bash

# Listar topics
kafka-topics --bootstrap-server localhost:9092 --list

# Deberías ver:
# - customer-created
# - customer-updated
# - customer-deleted
```

## 3. Probar el Flujo Completo

### Opción A: Usar el Script de Prueba

```bash
chmod +x test-kafka-events.sh
./test-kafka-events.sh
```

Este script:
1. Verifica que los servicios estén corriendo
2. Crea un cliente en Customer Service
3. Espera a que el evento se procese
4. Verifica que CustomerReference se creó en Account Service
5. Intenta crear una cuenta para validar que todo funciona

### Opción B: Prueba Manual

#### Paso 1: Crear un Cliente

```bash
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "gender": "MALE",
    "identification": "1234567890",
    "address": "Calle Principal 123",
    "phone": "+593999999999",
    "password": "password123",
    "status": "ACTIVE"
  }'
```

Guarda el `id` del cliente de la respuesta.

#### Paso 2: Verificar Logs de Customer Service

```bash
docker logs technique-customer-service --tail 50 | grep -i "publish\|event"
```

Deberías ver algo como:
```
Publishing CustomerCreatedEvent for customer ID: 1
CustomerCreatedEvent published successfully for customer ID: 1
```

#### Paso 3: Verificar Logs de Account Service

```bash
docker logs technique-account-service --tail 50 | grep -i "customer\|event"
```

Deberías ver algo como:
```
Received CustomerCreatedEvent for customer ID: 1
CustomerReference created for customer ID: 1
```

#### Paso 4: Verificar que CustomerReference se Creó

Puedes verificar directamente en la base de datos:

```bash
docker exec -it technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c "SELECT * FROM CUSTOMER_REFERENCE;"
```

O intentar crear una cuenta (esto validará que CustomerReference existe):

```bash
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

Si la cuenta se crea exitosamente, significa que:
- El evento se publicó correctamente
- El evento se consumió correctamente
- CustomerReference se creó/actualizó

## 4. Monitorear Eventos en Tiempo Real

### Ver logs en tiempo real

```bash
# Customer Service (productor)
docker logs -f technique-customer-service | grep -i "publish\|event"

# Account Service (consumidor)
docker logs -f technique-account-service | grep -i "customer\|event\|reference"
```

### Usar un consumidor de Kafka para ver eventos

```bash
# Entrar al contenedor de Kafka
docker exec -it technique-kafka bash

# Consumir eventos del topic customer-created
kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic customer-created \
  --from-beginning \
  --property print.key=true \
  --property print.value=true
```

En otra terminal, crea un cliente y verás el evento en tiempo real.

## 5. Probar Todos los Eventos

### Evento: Customer Created

```bash
# Crear cliente
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Created",
    "gender": "MALE",
    "identification": "1111111111",
    "address": "Test",
    "phone": "+593111111111",
    "password": "test",
    "status": "ACTIVE"
  }'

# Verificar en logs
docker logs technique-account-service --tail 20 | grep "CustomerCreatedEvent"
```

### Evento: Customer Updated

```bash
# Actualizar cliente (usa el ID del cliente creado)
curl -X PUT http://localhost:8081/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Updated",
    "gender": "MALE",
    "identification": "1111111111",
    "address": "Test Updated",
    "phone": "+593111111111",
    "password": "test",
    "status": "ACTIVE"
  }'

# Verificar en logs
docker logs technique-account-service --tail 20 | grep "CustomerUpdatedEvent"
```

### Evento: Customer Deleted

```bash
# Eliminar cliente (usa el ID del cliente creado)
curl -X DELETE http://localhost:8081/api/v1/customers/1

# Verificar en logs
docker logs technique-account-service --tail 20 | grep "CustomerDeletedEvent"
```

## 6. Verificar Errores Comunes

### El evento no se publica

**Síntomas:**
- No ves logs de "Publishing CustomerCreatedEvent" en Customer Service
- El evento no aparece en Kafka

**Solución:**
- Verifica que Kafka esté corriendo: `docker logs technique-kafka`
- Verifica la configuración de Kafka en `customer-service/src/main/resources/application.yaml`
- Verifica la conexión: `docker logs technique-customer-service | grep -i kafka`

### El evento no se consume

**Síntomas:**
- Ves el evento publicado pero no consumido
- No se crea CustomerReference

**Solución:**
- Verifica que Account Service esté corriendo: `docker logs technique-account-service`
- Verifica la configuración de Kafka en `account-service/src/main/resources/application.yaml`
- Verifica que el group-id sea correcto: `account-service-group`
- Revisa errores en los logs: `docker logs technique-account-service | grep -i error`

### CustomerReference no se crea

**Síntomas:**
- El evento se consume pero no se crea el registro

**Solución:**
- Verifica la conexión a la base de datos
- Verifica que la tabla CUSTOMER_REFERENCE exista
- Revisa errores de JPA: `docker logs technique-account-service | grep -i "error\|exception"`

## 7. Métricas y Monitoreo

### Ver métricas de Kafka

```bash
# Entrar al contenedor
docker exec -it technique-kafka bash

# Ver información del broker
kafka-broker-api-versions --bootstrap-server localhost:9092

# Ver información de los topics
kafka-topics --bootstrap-server localhost:9092 --describe
```

### Health Checks

```bash
# Customer Service
curl http://localhost:8081/actuator/health

# Account Service
curl http://localhost:8082/actuator/health
```

## 8. Troubleshooting

### Reiniciar un servicio específico

```bash
docker-compose restart customer-service
docker-compose restart account-service
```

### Ver todos los logs juntos

```bash
docker-compose logs -f
```

### Limpiar y reiniciar todo

```bash
docker-compose down -v
docker-compose up -d
```

**Nota:** Esto eliminará todos los datos y eventos.


