package cl.pagoflex.compromisos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ServiceTokenFilter serviceTokenFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/compromisos")
                        .hasAnyRole("ADMIN", "SERVICE")
                        .requestMatchers(HttpMethod.GET, "/api/compromisos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/compromisos")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA", "SERVICE")
                        .requestMatchers(HttpMethod.PUT, "/api/compromisos/*")
                        .hasAnyRole("ADMIN", "CLIENTE", "EMPRESA", "SERVICE")
                        .requestMatchers(HttpMethod.PATCH, "/api/compromisos/*/pagar")
                        .hasAnyRole("ADMIN", "SERVICE")
                        .requestMatchers(HttpMethod.PATCH, "/api/compromisos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/compromisos/**").hasRole("ADMIN")
                        .requestMatchers("/api/compromisos/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter())))
                .addFilterBefore(serviceTokenFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private JwtAuthenticationConverter jwtConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String rol = jwt.getClaimAsString("rol");
            return rol == null ? List.of() : List.of(new SimpleGrantedAuthority("ROLE_" + rol));
        });
        return converter;
    }
}
