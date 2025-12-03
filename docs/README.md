# Especificaciones OpenAPI

Esta carpeta contiene las especificaciones OpenAPI (Swagger) de los microservicios.

## Archivos

- `customer-service-openapi.yaml` - Especificación OpenAPI del Customer Service
- `account-service-openapi.yaml` - Especificación OpenAPI del Account Service

## Cómo generar los archivos

### Opción 1: Usando el script automático

```bash
# Asegúrate de que los servicios estén corriendo
docker-compose up -d

# Ejecutar el script
./generate-openapi.sh
```

El script esperará automáticamente a que los servicios estén disponibles y descargará los archivos OpenAPI.

### Opción 2: Generación manual

```bash
# Crear el directorio si no existe
mkdir -p docs

# Descargar Customer Service OpenAPI
curl http://localhost:8081/api/v1/api-docs.yaml -o docs/customer-service-openapi.yaml

# Descargar Account Service OpenAPI
curl http://localhost:8082/api/v1/api-docs.yaml -o docs/account-service-openapi.yaml
```

## Ver las especificaciones

Puedes ver las especificaciones en el navegador usando Swagger UI:

- **Customer Service**: http://localhost:8081/api/v1/swagger-ui.html
- **Account Service**: http://localhost:8082/api/v1/swagger-ui.html

O puedes usar herramientas como:
- [Swagger Editor](https://editor.swagger.io/) - Carga el archivo YAML
- [Postman](https://www.postman.com/) - Importa el archivo OpenAPI
- [Insomnia](https://insomnia.rest/) - Importa el archivo OpenAPI

## Notas

- Los archivos se generan automáticamente cuando los servicios están corriendo
- Las especificaciones se actualizan automáticamente cuando cambias los endpoints
- Los archivos están en formato YAML (OpenAPI 3.0)

