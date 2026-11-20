package cl.pagoflex.compromisos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:compromisos_context;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class MsCompromisosApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("ms-compromisos");
        assertThat(environment.getProperty("eureka.client.register-with-eureka", Boolean.class)).isTrue();
        assertThat(environment.getProperty("eureka.client.fetch-registry", Boolean.class)).isTrue();
        assertThat(environment.getProperty("eureka.client.service-url.defaultZone"))
                .isEqualTo("http://localhost:8761/eureka/");
        assertThat(environment.getProperty("eureka.instance.prefer-ip-address", Boolean.class)).isTrue();
    }
}
