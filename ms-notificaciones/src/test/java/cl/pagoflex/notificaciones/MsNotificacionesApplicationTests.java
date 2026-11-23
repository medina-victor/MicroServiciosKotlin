package cl.pagoflex.notificaciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "services.internal-token=PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789",
        "eureka.client.enabled=false",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password",
        "mail.from=test@example.com"
})
class MsNotificacionesApplicationTests {

	@Test
	void contextLoads() {
	}

}
