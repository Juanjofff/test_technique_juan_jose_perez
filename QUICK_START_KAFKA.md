# Inicio Rápido - Validar Eventos Kafka

## Pasos Rápidos para Validar

### 1. Levantar Servicios

```bash
docker-compose up -d
```

Espera unos segundos a que todos los servicios estén listos:

```bash
# Verificar que todos estén corriendo
docker-compose ps

# Deberías ver todos los servicios como "Up"
```

### 2. Ejecutar Script de Prueba Automático

```bash
./test-kafka-events.sh
```

Este script hará todo automáticamente y te mostrará si funciona o no.

### 3. Prueba Manual Rápida

#### Crear un Cliente

```bash
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Kafka",
    "gender": "MALE",
    "identification": "1234567890",
    "address": "Test",
    "phone": "+593999999999",
    "password": "test123",
    "status": "ACTIVE"
  }'
```

**Guarda el `id` de la respuesta** (ejemplo: `{"id": 1, ...}`)

#### Verificar que el Evento se Procesó

```bash
# Ver logs de Account Service (debería mostrar que consumió el evento)
docker logs technique-account-service --tail 30 | grep -i "customer\|event"
```

Deberías ver algo como:
```
Received CustomerCreatedEvent for customer ID: 1
CustomerReference created for customer ID: 1
```

#### Crear una Cuenta (Valida que CustomerReference Existe)

```bash
# Reemplaza 1 con el ID del cliente que obtuviste
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

Si la cuenta se crea exitosamente, **¡los eventos funcionan correctamente!** 

## Verificación Visual

### Ver Eventos en Tiempo Real

En una terminal, ejecuta:

```bash
docker logs -f technique-account-service | grep -i "customer\|event"
```

En otra terminal, crea/actualiza/elimina clientes y verás los eventos en tiempo real.

### Ver Logs de Kafka

```bash
docker logs -f technique-kafka
```

## Comandos Útiles

```bash
# Ver todos los logs juntos
docker-compose logs -f

# Reiniciar un servicio específico
docker-compose restart customer-service
docker-compose restart account-service

# Ver estado de los servicios
docker-compose ps

# Detener todo
docker-compose down

# Detener y eliminar volúmenes (limpia todo)
docker-compose down -v
```

## Solución de Problemas Rápidos

### Los servicios no inician

```bash
# Ver qué está fallando
docker-compose logs

# Reiniciar todo
docker-compose down
docker-compose up -d
```

### Kafka no está funcionando

```bash
# Ver logs de Kafka
docker logs technique-kafka

# Verificar que Kafka esté saludable
docker exec -it technique-kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### El evento no se consume

1. Verifica que Account Service esté corriendo: `docker logs technique-account-service`
2. Verifica que Customer Service publicó el evento: `docker logs technique-customer-service | grep -i publish`
3. Verifica la conexión a Kafka en ambos servicios

## Checklist de Validación

- [ ] Todos los servicios están corriendo (`docker-compose ps`)
- [ ] Puedo crear un cliente en Customer Service (puerto 8081)
- [ ] Veo el log "Publishing CustomerCreatedEvent" en Customer Service
- [ ] Veo el log "Received CustomerCreatedEvent" en Account Service
- [ ] Puedo crear una cuenta en Account Service (puerto 8082) para ese cliente
- [ ] Al actualizar un cliente, veo "CustomerUpdatedEvent" en ambos servicios
- [ ] Al eliminar un cliente, veo "CustomerDeletedEvent" en ambos servicios

Si todos los items están marcados, **¡los eventos de Kafka funcionan correctamente!**

