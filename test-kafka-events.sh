#!/bin/bash

echo "=========================================="
echo "Test de Eventos Kafka - Microservicios"
echo "=========================================="
echo ""

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Verificar que los servicios estén corriendo
echo "1. Verificando que los servicios estén corriendo..."
echo ""

CUSTOMER_SERVICE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)
ACCOUNT_SERVICE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/actuator/health)

if [ "$CUSTOMER_SERVICE" != "200" ]; then
    echo -e "${RED}✗ Customer Service no está disponible en http://localhost:8081${NC}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
else
    echo -e "${GREEN}✓ Customer Service está disponible${NC}"
fi

if [ "$ACCOUNT_SERVICE" != "200" ]; then
    echo -e "${RED}✗ Account Service no está disponible en http://localhost:8082${NC}"
    echo "   Ejecuta: docker-compose up -d"
    exit 1
else
    echo -e "${GREEN}✓ Account Service está disponible${NC}"
fi

echo ""
echo "2. Creando un cliente de prueba..."
echo ""

# Crear un cliente
CUSTOMER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Kafka Customer",
    "gender": "MALE",
    "identification": "9999999999",
    "address": "Test Address",
    "phone": "+593999999999",
    "password": "test123",
    "status": "ACTIVE"
  }')

CUSTOMER_ID=$(echo $CUSTOMER_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$CUSTOMER_ID" ]; then
    echo -e "${RED}✗ Error al crear cliente${NC}"
    echo "Response: $CUSTOMER_RESPONSE"
    exit 1
else
    echo -e "${GREEN}✓ Cliente creado con ID: $CUSTOMER_ID${NC}"
fi

echo ""
echo "3. Esperando 3 segundos para que el evento se procese..."
sleep 3

echo ""
echo "4. Verificando que CustomerReference se creó en Account Service..."
echo ""

# Verificar logs de account-service para ver si consumió el evento
ACCOUNT_LOGS=$(docker logs technique-account-service --tail 20 2>&1 | grep -i "CustomerCreatedEvent\|CustomerReference created")

if [ -z "$ACCOUNT_LOGS" ]; then
    echo -e "${YELLOW}⚠ No se encontraron logs del evento en Account Service${NC}"
    echo "   Revisa los logs manualmente: docker logs technique-account-service"
else
    echo -e "${GREEN}✓ Evento consumido correctamente${NC}"
    echo "   Logs encontrados:"
    echo "$ACCOUNT_LOGS" | head -3
fi

echo ""
echo "5. Intentando crear una cuenta para el cliente (valida CustomerReference)..."
echo ""

ACCOUNT_RESPONSE=$(curl -s -X POST http://localhost:8082/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d "{
    \"number\": \"TEST-$(date +%s)\",
    \"accountType\": \"AHORROS\",
    \"initialBalance\": 1000.00,
    \"status\": \"ACTIVE\",
    \"customerId\": $CUSTOMER_ID
  }")

if echo "$ACCOUNT_RESPONSE" | grep -q '"id"'; then
    echo -e "${GREEN}✓ Cuenta creada exitosamente${NC}"
    echo "   Esto confirma que CustomerReference existe y el evento funcionó"
else
    echo -e "${RED}✗ Error al crear cuenta${NC}"
    echo "   Response: $ACCOUNT_RESPONSE"
    echo ""
    echo "   Esto podría indicar que:"
    echo "   - El evento no se procesó correctamente"
    echo "   - CustomerReference no se creó"
    echo "   - Hay un error en AccountService"
fi

echo ""
echo "=========================================="
echo "Test completado"
echo "=========================================="
echo ""
echo "Para ver más detalles:"
echo "  - Logs de Customer Service: docker logs technique-customer-service"
echo "  - Logs de Account Service: docker logs technique-account-service"
echo "  - Logs de Kafka: docker logs technique-kafka"
echo ""

