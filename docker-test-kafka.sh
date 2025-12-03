#!/bin/bash

# Script para probar eventos de Kafka en Docker
# Uso: ./docker-test-kafka.sh

set -e

echo "=========================================="
echo "Test de Eventos Kafka en Docker"
echo "=========================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Verificar que Docker esté corriendo
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker no está corriendo${NC}"
    exit 1
fi

# Verificar que los servicios estén corriendo
echo -e "${BLUE}1. Verificando servicios Docker...${NC}"
echo ""

SERVICES=("technique-postgres" "technique-kafka" "technique-customer-service" "technique-account-service")
ALL_RUNNING=true

for service in "${SERVICES[@]}"; do
    if docker ps --format '{{.Names}}' | grep -q "^${service}$"; then
        echo -e "${GREEN}✓ ${service} está corriendo${NC}"
    else
        echo -e "${RED}✗ ${service} NO está corriendo${NC}"
        ALL_RUNNING=false
    fi
done

if [ "$ALL_RUNNING" = false ]; then
    echo ""
    echo -e "${YELLOW}Algunos servicios no están corriendo. Iniciando servicios...${NC}"
    docker-compose up -d
    echo ""
    echo "Esperando 15 segundos para que los servicios inicien..."
    sleep 15
fi

echo ""
echo -e "${BLUE}2. Verificando health checks...${NC}"
echo ""

# Verificar Customer Service
CUSTOMER_HEALTH=$(docker exec technique-customer-service curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/actuator/health 2>/dev/null || echo "000")
if [ "$CUSTOMER_HEALTH" = "200" ]; then
    echo -e "${GREEN}✓ Customer Service está saludable${NC}"
else
    echo -e "${RED}✗ Customer Service no responde (código: $CUSTOMER_HEALTH)${NC}"
    echo "   Revisa logs: docker logs technique-customer-service"
    exit 1
fi

# Verificar Account Service
ACCOUNT_HEALTH=$(docker exec technique-account-service curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/actuator/health 2>/dev/null || echo "000")
if [ "$ACCOUNT_HEALTH" = "200" ]; then
    echo -e "${GREEN}✓ Account Service está saludable${NC}"
else
    echo -e "${RED}✗ Account Service no responde (código: $ACCOUNT_HEALTH)${NC}"
    echo "   Revisa logs: docker logs technique-account-service"
    exit 1
fi

echo ""
echo -e "${BLUE}3. Verificando que Kafka esté funcionando...${NC}"
echo ""

# Verificar Kafka
if docker exec technique-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka está funcionando${NC}"
else
    echo -e "${RED}✗ Kafka no está respondiendo${NC}"
    echo "   Revisa logs: docker logs technique-kafka"
    exit 1
fi

# Verificar topics
echo ""
echo -e "${BLUE}4. Verificando topics de Kafka...${NC}"
echo ""

TOPICS=$(docker exec technique-kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "")
REQUIRED_TOPICS=("customer-created" "customer-updated" "customer-deleted")

for topic in "${REQUIRED_TOPICS[@]}"; do
    if echo "$TOPICS" | grep -q "^${topic}$"; then
        echo -e "${GREEN}✓ Topic '${topic}' existe${NC}"
    else
        echo -e "${YELLOW}⚠ Topic '${topic}' no existe (se creará automáticamente)${NC}"
    fi
done

echo ""
echo -e "${BLUE}5. Limpiando datos de prueba anteriores...${NC}"
echo ""

# Limpiar CustomerReference de pruebas anteriores (opcional, comentado por seguridad)
# docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c "DELETE FROM CUSTOMER_REFERENCE WHERE IDENTIFICACION LIKE 'TEST-%';" 2>/dev/null || true

echo ""
echo -e "${BLUE}6. Creando cliente de prueba...${NC}"
echo ""

TIMESTAMP=$(date +%s)
CUSTOMER_DATA=$(cat <<EOF
{
  "name": "Test Kafka Customer ${TIMESTAMP}",
  "gender": "MALE",
  "identification": "TEST-${TIMESTAMP}",
  "address": "Test Address",
  "phone": "+593999999999",
  "password": "test123",
  "status": "ACTIVE"
}
EOF
)

CUSTOMER_RESPONSE=$(docker exec technique-customer-service curl -s -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d "${CUSTOMER_DATA}")

CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")

if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}✗ Error al crear cliente${NC}"
    echo "Response: $CUSTOMER_RESPONSE"
    exit 1
else
    echo -e "${GREEN}✓ Cliente creado con ID: ${CUSTOMER_ID}${NC}"
    echo "   Response: $(echo "$CUSTOMER_RESPONSE" | jq -c '.' 2>/dev/null || echo "$CUSTOMER_RESPONSE")"
fi

echo ""
echo -e "${BLUE}7. Esperando 5 segundos para que el evento se procese...${NC}"
sleep 5

echo ""
echo -e "${BLUE}8. Verificando logs de Customer Service (evento publicado)...${NC}"
echo ""

CUSTOMER_LOGS=$(docker logs technique-customer-service --tail 50 2>&1 | grep -i "CustomerCreatedEvent\|publish" | tail -3)
if [ -z "$CUSTOMER_LOGS" ]; then
    echo -e "${YELLOW}⚠ No se encontraron logs de publicación del evento${NC}"
else
    echo -e "${GREEN}✓ Evento publicado:${NC}"
    echo "$CUSTOMER_LOGS" | while read line; do
        echo "   $line"
    done
fi

echo ""
echo -e "${BLUE}9. Verificando logs de Account Service (evento consumido)...${NC}"
echo ""

ACCOUNT_LOGS=$(docker logs technique-account-service --tail 50 2>&1 | grep -i "CustomerCreatedEvent\|CustomerReference created" | tail -3)
if [ -z "$ACCOUNT_LOGS" ]; then
    echo -e "${RED}✗ No se encontraron logs de consumo del evento${NC}"
    echo "   Esto podría indicar que el evento no se consumió correctamente"
    echo "   Revisa logs completos: docker logs technique-account-service --tail 100"
else
    echo -e "${GREEN}✓ Evento consumido:${NC}"
    echo "$ACCOUNT_LOGS" | while read line; do
        echo "   $line"
    done
fi

echo ""
echo -e "${BLUE}10. Verificando que CustomerReference se creó en la base de datos...${NC}"
echo ""

DB_CHECK=$(docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -t -c "SELECT COUNT(*) FROM CUSTOMER_REFERENCE WHERE ID_CLIENTE = ${CUSTOMER_ID};" 2>/dev/null | tr -d ' ' || echo "0")

if [ "$DB_CHECK" = "1" ]; then
    echo -e "${GREEN}✓ CustomerReference existe en la base de datos${NC}"
    
    # Mostrar datos
    CUSTOMER_REF_DATA=$(docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c "SELECT * FROM CUSTOMER_REFERENCE WHERE ID_CLIENTE = ${CUSTOMER_ID};" 2>/dev/null)
    echo "$CUSTOMER_REF_DATA"
else
    echo -e "${RED}✗ CustomerReference NO existe en la base de datos${NC}"
    echo "   Esto indica que el evento no se procesó correctamente"
fi

echo ""
echo -e "${BLUE}11. Intentando crear una cuenta para validar el flujo completo...${NC}"
echo ""

ACCOUNT_DATA=$(cat <<EOF
{
  "number": "TEST-ACC-${TIMESTAMP}",
  "accountType": "AHORROS",
  "initialBalance": 1000.00,
  "status": "ACTIVE",
  "customerId": ${CUSTOMER_ID}
}
EOF
)

ACCOUNT_RESPONSE=$(docker exec technique-account-service curl -s -X POST http://localhost:8082/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d "${ACCOUNT_DATA}")

if echo "$ACCOUNT_RESPONSE" | grep -q '"id"'; then
    ACCOUNT_ID=$(echo "$ACCOUNT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${GREEN}✓ Cuenta creada exitosamente con ID: ${ACCOUNT_ID}${NC}"
    echo "   Esto confirma que:"
    echo "   - El evento se publicó correctamente"
    echo "   - El evento se consumió correctamente"
    echo "   - CustomerReference se creó correctamente"
    echo "   - Account Service puede validar clientes"
else
    echo -e "${RED}✗ Error al crear cuenta${NC}"
    echo "   Response: $ACCOUNT_RESPONSE"
    echo ""
    echo "   Posibles causas:"
    echo "   - CustomerReference no existe"
    echo "   - Error en AccountService"
    echo "   - Error de validación"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}Test completado${NC}"
echo "=========================================="
echo ""
echo "Comandos útiles para debugging:"
echo "  - Ver logs de Customer Service: docker logs technique-customer-service --tail 50"
echo "  - Ver logs de Account Service: docker logs technique-account-service --tail 50"
echo "  - Ver logs de Kafka: docker logs technique-kafka --tail 50"
echo "  - Ver CustomerReference en DB: docker exec technique-postgres psql -U myuser -d test_technique_juan_jose_perez -c 'SELECT * FROM CUSTOMER_REFERENCE;'"
echo "  - Consumir eventos manualmente: docker exec -it technique-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic customer-created --from-beginning"
echo ""

