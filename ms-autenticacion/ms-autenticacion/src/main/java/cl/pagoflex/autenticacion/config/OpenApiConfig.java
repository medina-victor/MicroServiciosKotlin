package cl.pagoflex.autenticacion.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI autenticacionOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API ms-autenticacion - PagoFlex")
                .version("1.0.0")
                .description("Registro seguro de credenciales, autenticación y generación de JWT."));
    }
}
