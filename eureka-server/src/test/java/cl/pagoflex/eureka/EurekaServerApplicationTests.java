package cl.pagoflex.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EurekaServerApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertThat(environment.getProperty("spring.application.name")).isEqualTo("eureka-server");
		assertThat(environment.getProperty("server.port", Integer.class)).isEqualTo(8761);
		assertThat(environment.getProperty("eureka.client.register-with-eureka", Boolean.class)).isFalse();
		assertThat(environment.getProperty("eureka.client.fetch-registry", Boolean.class)).isFalse();
		assertThat(environment.getProperty("eureka.server.enable-self-preservation", Boolean.class)).isTrue();
		assertThat(environment.getProperty("eureka.instance.hostname")).isEqualTo("localhost");
	}

}
