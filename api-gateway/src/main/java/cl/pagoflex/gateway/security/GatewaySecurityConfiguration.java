package cl.pagoflex.gateway.security;

import cl.pagoflex.gateway.exception.GatewayErrorResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import reactor.core.publisher.Mono;

@Configuration
public class GatewaySecurityConfiguration {
    @Bean
    SecurityWebFilterChain gatewaySecurityWebFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter,
            GatewayErrorResponseWriter responseWriter) {
        ServerAuthenticationEntryPoint authenticationEntryPoint =
                (exchange, exception) -> responseWriter.write(exchange, HttpStatus.UNAUTHORIZED);
        ServerAccessDeniedHandler accessDeniedHandler =
                (exchange, exception) -> responseWriter.write(exchange, HttpStatus.FORBIDDEN);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeExchange(authorize -> authorize
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login",
                                "/api/auth/recuperacion/solicitar", "/api/auth/recuperacion/cambiar",
                                "/api/usuarios")
                        .permitAll()
                        .pathMatchers("/actuator/health", "/actuator/health/**", "/actuator/info")
                        .permitAll()
                        .pathMatchers("/actuator/gateway/**").hasRole("ADMIN")
                        .pathMatchers("/actuator/**").denyAll()

                        .pathMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/rol/**")
                        .hasRole("ADMIN")
                        .pathMatchers("/api/usuarios/**").hasRole("ADMIN")

                        .pathMatchers("/api/empresas/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/compromisos").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/compromisos/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/compromisos")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA")
                        .pathMatchers(HttpMethod.PUT, "/api/compromisos/**")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA")
                        .pathMatchers(HttpMethod.PATCH, "/api/compromisos/*/pagar").hasRole("ADMIN")
                        .pathMatchers("/api/compromisos/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/pagos").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/pagos/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/pagos")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA")
                        .pathMatchers(HttpMethod.PATCH, "/api/pagos/*/exitoso").hasRole("ADMIN")
                        .pathMatchers("/api/pagos/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.GET, "/api/soporte").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/soporte/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/soporte")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA")
                        .pathMatchers("/api/soporte/**").hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/auth/desactivar/**").hasRole("ADMIN")
                        .pathMatchers("/api/auth/**").authenticated()
                        .anyExchange().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }
}
