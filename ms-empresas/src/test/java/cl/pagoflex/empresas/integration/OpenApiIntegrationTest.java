package cl.pagoflex.empresas.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:empresas_openapi;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class OpenApiIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configuraRegistroEureka() {
        org.assertj.core.api.Assertions.assertThat(environment.getProperty("spring.application.name"))
                .isEqualTo("ms-empresas");
        org.assertj.core.api.Assertions.assertThat(environment.getProperty(
                "eureka.client.register-with-eureka", Boolean.class)).isTrue();
        org.assertj.core.api.Assertions.assertThat(environment.getProperty(
                "eureka.client.fetch-registry", Boolean.class)).isTrue();
        org.assertj.core.api.Assertions.assertThat(environment.getProperty(
                "eureka.client.service-url.defaultZone")).isEqualTo("http://localhost:8761/eureka/");
        org.assertj.core.api.Assertions.assertThat(environment.getProperty(
                "eureka.instance.prefer-ip-address", Boolean.class)).isTrue();
    }

    @Test
    void publicaSwaggerUiYEspecificacion() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ms-empresas - PagoFlex"))
                .andExpect(jsonPath("$.paths['/api/empresas']").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
