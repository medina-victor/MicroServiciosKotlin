package cl.pagoflex.soporte.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI soporteOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API ms-soporte - PagoFlex")
                .version("1.0.0")
                .description("API para registrar, consultar, responder y cerrar solicitudes de soporte."));
    }
}
