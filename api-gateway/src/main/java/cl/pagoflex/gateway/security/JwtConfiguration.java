package cl.pagoflex.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Configuration
public class JwtConfiguration {
    private static final Set<String> ALLOWED_ROLES = Set.of("CLIENTE", "EMPRESA", "ADMIN");

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder(@Value("${gateway.security.jwt-secret}") String secret) {
        if (secret == null || secret.isBlank()
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe configurarse en el entorno con al menos 32 bytes y el mismo valor en todos los servicios");
        }

        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> requiredClaimsValidator = jwt -> {
            Object userId = jwt.getClaims().get("usuarioId");
            String role = jwt.getClaimAsString("rol");
            Object companyId = jwt.getClaims().get("empresaId");
            boolean validUserId = userId instanceof Number number && number.longValue() > 0;
            boolean validCompanyId = companyId instanceof Number number && number.longValue() > 0;

            if (!validUserId || role == null || !ALLOWED_ROLES.contains(role)
                    || ("EMPRESA".equals(role) && !validCompanyId)) {
                return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Required PagoFlex claims are invalid", null));
            }
            return OAuth2TokenValidatorResult.success();
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), requiredClaimsValidator));
        return decoder;
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            String role = jwt.getClaimAsString("rol");
            List<GrantedAuthority> authorities = ALLOWED_ROLES.contains(role)
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    : List.of();
            return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
        };
    }
}
