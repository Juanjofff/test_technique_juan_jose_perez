#!/bin/bash

# Script para ejecutar dentro de un contenedor Docker
# Uso: docker exec -it technique-customer-service bash -c "curl -o /tmp/test.sh https://... && bash /tmp/test.sh"
# O: docker cp test-inside-docker.sh technique-customer-service:/tmp/ && docker exec -it technique-customer-service bash /tmp/test-inside-docker.sh

set -e

echo "=========================================="
echo "Test de Eventos Kafka - Desde Contenedor"
echo "=========================================="
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Detectar en qué servicio estamos
if [ -n "$SPRING_APPLICATION_NAME" ]; then
    SERVICE_NAME="$SPRING_APPLICATION_NAME"
else
    SERVICE_NAME="unknown"
fi

echo -e "${BLUE}Ejecutando desde: ${SERVICE_NAME}${NC}"
echo ""

# Verificar conectividad
echo -e "${BLUE}1. Verificando conectividad...${NC}"
echo ""

# Verificar Customer Service
if curl -s -f http://customer-service:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Customer Service accesible${NC}"
    CUSTOMER_SERVICE_URL="http://customer-service:8081"
elif curl -s -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Customer Service accesible (localhost)${NC}"
    CUSTOMER_SERVICE_URL="http://localhost:8081"
else
    echo -e "${RED}✗ Customer Service no accesible${NC}"
    CUSTOMER_SERVICE_URL=""
fi

# Verificar Account Service
if curl -s -f http://account-service:8082/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Account Service accesible${NC}"
    ACCOUNT_SERVICE_URL="http://account-service:8082"
elif curl -s -f http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Account Service accesible (localhost)${NC}"
    ACCOUNT_SERVICE_URL="http://localhost:8082"
else
    echo -e "${RED}✗ Account Service no accesible${NC}"
    ACCOUNT_SERVICE_URL=""
fi

if [ -z "$CUSTOMER_SERVICE_URL" ] || [ -z "$ACCOUNT_SERVICE_URL" ]; then
    echo -e "${RED}No se pueden acceder a los servicios${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}2. Creando cliente de prueba...${NC}"
echo ""

TIMESTAMP=$(date +%s)
CUSTOMER_DATA=$(cat <<EOF
{
  "name": "Test Kafka Docker ${TIMESTAMP}",
  "gender": "MALE",
  "identification": "DOCKER-TEST-${TIMESTAMP}",
  "address": "Test Address",
  "phone": "+593999999999",
  "password": "test123",
  "status": "ACTIVE"
}
EOF
)

CUSTOMER_RESPONSE=$(curl -s -X POST ${CUSTOMER_SERVICE_URL}/api/v1/customers \
  -H "Content-Type: application/json" \
  -d "${CUSTOMER_DATA}")

CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2 || echo "")

if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}✗ Error al crear cliente${NC}"
    echo "Response: $CUSTOMER_RESPONSE"
    exit 1
else
    echo -e "${GREEN}✓ Cliente creado con ID: ${CUSTOMER_ID}${NC}"
fi

echo ""
echo -e "${BLUE}3. Esperando 5 segundos para procesamiento del evento...${NC}"
sleep 5

echo ""
echo -e "${BLUE}4. Verificando que se puede crear una cuenta...${NC}"
echo ""

ACCOUNT_DATA=$(cat <<EOF
{
  "number": "DOCKER-TEST-${TIMESTAMP}",
  "accountType": "AHORROS",
  "initialBalance": 1000.00,
  "status": "ACTIVE",
  "customerId": ${CUSTOMER_ID}
}
EOF
)

ACCOUNT_RESPONSE=$(curl -s -X POST ${ACCOUNT_SERVICE_URL}/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d "${ACCOUNT_DATA}")

if echo "$ACCOUNT_RESPONSE" | grep -q '"id"'; then
    ACCOUNT_ID=$(echo "$ACCOUNT_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    echo -e "${GREEN}✓ Cuenta creada exitosamente con ID: ${ACCOUNT_ID}${NC}"
    echo ""
    echo -e "${GREEN}✅ TEST EXITOSO: Los eventos de Kafka funcionan correctamente${NC}"
    exit 0
else
    echo -e "${RED}✗ Error al crear cuenta${NC}"
    echo "Response: $ACCOUNT_RESPONSE"
    echo ""
    echo -e "${RED}❌ TEST FALLIDO: El evento no se procesó correctamente${NC}"
    exit 1
fi

