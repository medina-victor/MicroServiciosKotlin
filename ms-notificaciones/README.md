# ms-notificaciones

Microservicio de PagoFlex responsable exclusivamente del envio de correos electronicos.

## Arquitectura

- Java 21
- Spring Boot 3.5.4
- Spring Cloud 2025.0.0
- Eureka Discovery Client
- JavaMailSender con SMTP Gmail
- Spring Security para autenticacion interna
- Springdoc OpenAPI 2.8.9

## Ejecucion

```powershell
cd ms-notificaciones
set INTERNAL_SERVICE_TOKEN=un-token-compartido-de-al-menos-32-bytes
set MAIL_HOST=smtp.gmail.com
set MAIL_PORT=587
set MAIL_USERNAME=tu-cuenta@gmail.com
set MAIL_PASSWORD=tu-app-password
set MAIL_FROM=tu-cuenta@gmail.com
.\mvnw.cmd spring-boot:run
```

Puerto: `8089`

Eureka: `http://localhost:8761/eureka/`

Swagger: `http://localhost:8089/swagger-ui/index.html`

## Variables de entorno

| Variable | Obligatoria | Descripcion |
|---|---:|---|
| `INTERNAL_SERVICE_TOKEN` | Si | Token compartido, minimo 32 bytes |
| `MAIL_HOST` | Si | Predeterminado `smtp.gmail.com` |
| `MAIL_PORT` | Si | Predeterminado `587` |
| `MAIL_USERNAME` | Si | Cuenta Gmail remitente |
| `MAIL_PASSWORD` | Si | App Password de Gmail |
| `MAIL_FROM` | Si | Direccion remitente |
| `EUREKA_DEFAULT_ZONE` | No | URL de Eureka |
| `EUREKA_CLIENT_ENABLED` | No | Predeterminado `true` |

No utilice la contraseña normal de Gmail. Cree una App Password siguiendo la configuracion oficial de Google.

## Endpoints internos

Todos requieren el encabezado:

```text
X-Internal-Token: <INTERNAL_SERVICE_TOKEN>
```

### Enviar codigo de recuperacion

```http
POST /api/notificaciones/recuperacion
Content-Type: application/json
X-Internal-Token: <INTERNAL_SERVICE_TOKEN>
```

```json
{
  "correo": "usuario@example.com",
  "codigo": "123456"
}
```

### Confirmar cambio de contrasena

```http
POST /api/notificaciones/cambio-contrasena
Content-Type: application/json
X-Internal-Token: <INTERNAL_SERVICE_TOKEN>
```

```json
{
  "correo": "usuario@example.com"
}
```

El servicio no genera ni almacena codigos, no cambia contrasenas y no recibe HTML libre.

## Respuestas

- `200`: correo enviado
- `400`: solicitud invalida
- `401`: token interno ausente
- `403`: token interno incorrecto
- `500`: error interno
- `503`: configuracion o conexion SMTP no disponible

## Pruebas

```powershell
cd ms-notificaciones
.\mvnw.cmd clean test
```

Las pruebas utilizan mocks de `JavaMailSender`; no requieren una cuenta Gmail real ni realizan envios reales.
