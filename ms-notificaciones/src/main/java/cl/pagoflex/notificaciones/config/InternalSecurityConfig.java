package cl.pagoflex.notificaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@Configuration
public class InternalSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                             cl.pagoflex.notificaciones.security.ServiceTokenFilter serviceTokenFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/actuator/health").permitAll()
                        .requestMatchers("/api/notificaciones/**").hasRole("SERVICE")
                        .anyRequest().authenticated())
                .addFilterBefore(serviceTokenFilter, RequestHeaderAuthenticationFilter.class);
        return http.build();
    }
}
