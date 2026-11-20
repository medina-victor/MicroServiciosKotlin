package cl.pagoflex.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApiGatewayApplicationTests {
	@Autowired
	private RouteLocator routeLocator;

	@Test
	void contextLoads() {
	}

	@Test
	void declaresManualRoutesForAllServices() {
		Set<String> routeIds = routeLocator.getRoutes()
				.map(route -> route.getId())
				.collectList()
				.blockOptional()
				.orElseThrow()
				.stream()
				.collect(java.util.stream.Collectors.toSet());

		assertThat(routeIds).containsExactlyInAnyOrder(
				"ms-usuarios",
				"ms-empresas",
				"ms-compromisos",
				"ms-pagos",
				"ms-soporte",
				"ms-autenticacion");
	}
}
