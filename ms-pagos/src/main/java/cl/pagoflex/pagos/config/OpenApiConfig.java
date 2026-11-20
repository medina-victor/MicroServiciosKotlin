package cl.pagoflex.pagos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pagosOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API ms-pagos - PagoFlex")
                .version("1.0.0")
                .description("API independiente para registrar pagos simulados. No procesa información financiera sensible."));
    }
}
