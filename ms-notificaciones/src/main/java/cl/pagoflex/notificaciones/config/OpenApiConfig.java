package cl.pagoflex.notificaciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificacionesOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("X-Internal-Token",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Token")
                                .description("Token compartido entre microservicios")))
                .info(new Info()
                        .title("API ms-notificaciones - PagoFlex")
                        .version("1.0.0")
                        .description("Servicio interno exclusivo para el envio de correos de PagoFlex."));
    }
}
