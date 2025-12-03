# Arquitectura de Microservicios

Este proyecto ha sido refactorizado para implementar una arquitectura de microservicios con comunicación asíncrona mediante Apache Kafka.

## Estructura

El proyecto está dividido en dos microservicios:

### 1. Customer Service (Puerto 8081)
- **Responsabilidad**: Gestión de Clientes y Personas
- **Endpoints**: `/api/v1/customers`
- **Base de datos**: Comparte la misma base de datos PostgreSQL (tabla `CLIENTE`)
- **Eventos publicados**:
  - `customer-created`: Cuando se crea un nuevo cliente
  - `customer-updated`: Cuando se actualiza un cliente
  - `customer-deleted`: Cuando se elimina un cliente

### 2. Account Service (Puerto 8082)
- **Responsabilidad**: Gestión de Cuentas, Movimientos y Reportes
- **Endpoints**: 
  - `/api/v1/accounts`
  - `/api/v1/movements`
  - `/api/v1/reports/{client-id}`
- **Base de datos**: Comparte la misma base de datos PostgreSQL (tablas `CUENTA`, `MOVIMIENTOS`, `CUSTOMER_REFERENCE`)
- **Eventos consumidos**: 
  - `customer-created`
  - `customer-updated`
  - `customer-deleted`

## Comunicación Asíncrona

La comunicación entre servicios se realiza mediante **Apache Kafka**:

- **Customer Service** actúa como **productor** de eventos cuando se crean, actualizan o eliminan clientes.
- **Account Service** actúa como **consumidor** de eventos y mantiene una copia desnormalizada de los datos del cliente en la tabla `CUSTOMER_REFERENCE` para garantizar consistencia eventual.

### Eventos de Dominio

Los eventos publicados contienen la siguiente información:

```java
CustomerCreatedEvent {
    Long customerId;
    String name;
    String identification;
    String status;
}
```

## Infraestructura

### Docker Compose

El archivo `compose.yaml` incluye:

1. **PostgreSQL**: Base de datos compartida
2. **Zookeeper**: Coordinador para Kafka
3. **Kafka**: Broker de mensajería
4. **Customer Service**: Microservicio de clientes
5. **Account Service**: Microservicio de cuentas

### Ejecución

Para levantar toda la infraestructura:

```bash
docker-compose up -d
```

Los servicios estarán disponibles en:
- Customer Service: http://localhost:8081
- Account Service: http://localhost:8082
- Kafka: localhost:9092
- PostgreSQL: localhost:5432

### Swagger UI

- Customer Service: http://localhost:8081/swagger-ui.html
- Account Service: http://localhost:8082/swagger-ui.html

## Flujo de Datos

1. **Creación de Cliente**:
   - Cliente crea un cliente mediante `POST /api/v1/customers` en Customer Service
   - Customer Service guarda el cliente en la base de datos
   - Customer Service publica evento `customer-created` en Kafka
   - Account Service consume el evento y crea/actualiza `CUSTOMER_REFERENCE`

2. **Creación de Cuenta**:
   - Cliente crea una cuenta mediante `POST /api/v1/accounts` en Account Service
   - Account Service valida que el cliente existe consultando `CUSTOMER_REFERENCE`
   - Si existe, crea la cuenta; si no, retorna error

3. **Actualización/Eliminación de Cliente**:
   - Similar al flujo de creación, pero con eventos `customer-updated` o `customer-deleted`
   - Account Service actualiza `CUSTOMER_REFERENCE` en consecuencia

## Consistencia de Datos

La arquitectura utiliza **consistencia eventual**:
- Los datos del cliente se replican de forma asíncrona desde Customer Service a Account Service
- Puede haber un pequeño retraso entre la creación/actualización del cliente y su disponibilidad en Account Service
- La tabla `CUSTOMER_REFERENCE` mantiene una copia desnormalizada para consultas rápidas
