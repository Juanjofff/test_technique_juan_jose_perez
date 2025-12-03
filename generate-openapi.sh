#!/bin/bash

# Script para generar los archivos OpenAPI YAML de los servicios
# Requiere que los servicios estén corriendo

set -e

CUSTOMER_SERVICE_URL="http://localhost:8081/api/v1/api-docs.yaml"
ACCOUNT_SERVICE_URL="http://localhost:8082/api/v1/api-docs.yaml"
DOCS_DIR="docs"

echo "Generando archivos OpenAPI..."

# Crear directorio docs si no existe
mkdir -p "$DOCS_DIR"

# Función para esperar a que un servicio esté disponible
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "Esperando a que $service_name esté disponible..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo "$service_name está disponible"
            return 0
        fi
        echo "Intento $attempt/$max_attempts: $service_name no está disponible, esperando..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo "Error: $service_name no está disponible después de $max_attempts intentos"
    return 1
}

# Generar OpenAPI de Customer Service
if wait_for_service "$CUSTOMER_SERVICE_URL" "Customer Service"; then
    echo "Descargando OpenAPI de Customer Service..."
    curl -s "$CUSTOMER_SERVICE_URL" -o "$DOCS_DIR/customer-service-openapi.yaml"
    if [ $? -eq 0 ]; then
        echo "✓ Customer Service OpenAPI generado: $DOCS_DIR/customer-service-openapi.yaml"
    else
        echo "✗ Error al generar Customer Service OpenAPI"
        exit 1
    fi
else
    echo "⚠ Saltando Customer Service (no disponible)"
fi

# Generar OpenAPI de Account Service
if wait_for_service "$ACCOUNT_SERVICE_URL" "Account Service"; then
    echo "Descargando OpenAPI de Account Service..."
    curl -s "$ACCOUNT_SERVICE_URL" -o "$DOCS_DIR/account-service-openapi.yaml"
    if [ $? -eq 0 ]; then
        echo "✓ Account Service OpenAPI generado: $DOCS_DIR/account-service-openapi.yaml"
    else
        echo "✗ Error al generar Account Service OpenAPI"
        exit 1
    fi
else
    echo "⚠ Saltando Account Service (no disponible)"
fi

echo ""
echo "✅ Archivos OpenAPI generados exitosamente en $DOCS_DIR/"
ls -lh "$DOCS_DIR"/*.yaml 2>/dev/null || echo "No se generaron archivos"

