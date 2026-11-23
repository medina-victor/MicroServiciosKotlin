package cl.pagoflex.usuarios.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtSecurityConfig {
    @Bean
    JwtDecoder jwtDecoder(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()
                || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe configurarse en el entorno con al menos 32 bytes y el mismo valor en todos los servicios");
        }
        return NimbusJwtDecoder.withSecretKey(
                new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256).build();
    }
}
