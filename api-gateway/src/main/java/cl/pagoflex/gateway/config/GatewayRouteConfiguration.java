package cl.pagoflex.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfiguration {
    @Bean
    RouteLocator pagoFlexRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ms-usuarios", route -> route.path("/api/usuarios/**").uri("lb://MS-USUARIOS"))
                .route("ms-empresas", route -> route.path("/api/empresas/**").uri("lb://MS-EMPRESAS"))
                .route("ms-compromisos", route -> route.path("/api/compromisos/**").uri("lb://MS-COMPROMISOS"))
                .route("ms-pagos", route -> route.path("/api/pagos/**").uri("lb://MS-PAGOS"))
                .route("ms-soporte", route -> route.path("/api/soporte/**").uri("lb://MS-SOPORTE"))
                .route("ms-autenticacion", route -> route.path("/api/auth/**").uri("lb://MS-AUTENTICACION"))
                .build();
    }
}
