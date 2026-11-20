# Eureka Server - PagoFlex

Eureka Server es un registro de servicios que permite que aplicaciones cliente publiquen su ubicación y descubran otros servicios registrados. En PagoFlex, este servidor central permitirá el descubrimiento dinámico de los microservicios cuando se configure Eureka Client en ellos.

## Tecnologías

- Java 21
- Spring Boot 3.5.4
- Spring Cloud 2025.0.0
- Spring Cloud Netflix Eureka Server
- Maven

La versión Spring Cloud 2025.0.x pertenece a la línea compatible con Spring Boot 3.5.x. El servidor no usa base de datos; Eureka conserva el registro en memoria.

## Puerto y panel web

- Puerto: `8761`
- Panel Eureka: http://localhost:8761/

## Iniciar el servidor

Desde la carpeta `eureka-server`:

```powershell
.\mvnw.cmd spring-boot:run
```

Ejecutar pruebas:

```powershell
.\mvnw.cmd clean test
```

## Integración futura de microservicios

Este proyecto solo proporciona el servidor. No modifica ni registra los microservicios PagoFlex por sí mismo. Cuando cada microservicio incluya y configure Eureka Client, aparecerá como instancia en la sección **Instances currently registered with Eureka** del panel. También se podrá consultar el registro en `/eureka/apps`.

No se requiere MySQL, H2 ni configuración Springdoc para el servidor Eureka.
