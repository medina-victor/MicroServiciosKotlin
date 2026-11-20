package cl.pagoflex.soporte;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:soporte_context;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MsSoporteApplicationTests {
    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("ms-soporte");
        assertThat(environment.getProperty("eureka.client.register-with-eureka", Boolean.class)).isTrue();
        assertThat(environment.getProperty("eureka.client.fetch-registry", Boolean.class)).isTrue();
        assertThat(environment.getProperty("eureka.client.service-url.defaultZone"))
                .isEqualTo("http://localhost:8761/eureka/");
        assertThat(environment.getProperty("eureka.instance.prefer-ip-address", Boolean.class)).isTrue();
    }
}
