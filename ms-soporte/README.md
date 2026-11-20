# ms-soporte

Microservicio de PagoFlex para registrar, consultar, responder y cerrar solicitudes de soporte.

## Tecnologías

- Java 21
- Spring Boot 3.5.4
- Spring Web y Spring Data JPA
- MySQL 8
- H2 para pruebas
- Springdoc OpenAPI / Swagger
- Maven

## Configuración

- Puerto: `8085`
- Base de datos: `soporte_db`
- Swagger UI: http://localhost:8085/swagger-ui/index.html
- OpenAPI: http://localhost:8085/v3/api-docs

Variables opcionales:

```text
DB_URL=jdbc:mysql://localhost:3306/soporte_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=
```

No se almacenan credenciales sensibles en el repositorio.

## Ejecutar

Desde `ms-soporte`:

```powershell
.\mvnw.cmd spring-boot:run
```

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd clean test
```

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/soporte` | Crear ticket |
| GET | `/api/soporte` | Listar tickets |
| GET | `/api/soporte/{id}` | Buscar por ID |
| GET | `/api/soporte/usuario/{usuarioId}` | Buscar por usuario |
| GET | `/api/soporte/empresa/{empresaId}` | Buscar por empresa |
| GET | `/api/soporte/estado/{estado}` | Buscar por estado |
| GET | `/api/soporte/categoria/{categoria}` | Buscar por categoría |
| GET | `/api/soporte/prioridad/{prioridad}` | Buscar por prioridad |
| PUT | `/api/soporte/{id}` | Actualizar información |
| PATCH | `/api/soporte/{id}/estado` | Cambiar estado |
| PATCH | `/api/soporte/{id}/responder` | Responder |
| PATCH | `/api/soporte/{id}/cerrar` | Cerrar |
| DELETE | `/api/soporte/{id}` | Eliminar ticket no cerrado |

## Ejemplos JSON

Crear ticket:

```json
{
  "usuarioId": 1,
  "empresaId": 2,
  "asunto": "Problema con un pago",
  "descripcion": "Realicé un pago pero todavía aparece pendiente en la aplicación.",
  "categoria": "PAGOS",
  "prioridad": "MEDIA"
}
```

Cambiar estado:

```json
{
  "estado": "EN_REVISION"
}
```

Responder:

```json
{
  "respuesta": "Estamos revisando la información de tu pago."
}
```
