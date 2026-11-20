package cl.pagoflex.autenticacion.security;

import cl.pagoflex.autenticacion.exception.TokenInvalidoException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final String secret;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms:3600000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.jwtEncoder = parameters -> {
            try {
                Map<String, Object> claims = parameters.getClaims().getClaims();
                JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                        .subject((String) claims.get("sub"));
                Object issuedAtClaim = claims.get("iat");
                Object expiresAtClaim = claims.get("exp");
                Instant claimIssuedAt = issuedAtClaim instanceof Instant instant
                        ? instant : Instant.parse(issuedAtClaim.toString());
                Instant claimExpiresAt = expiresAtClaim instanceof Instant instant
                        ? instant : Instant.parse(expiresAtClaim.toString());
                claimsBuilder.issueTime(Date.from(claimIssuedAt))
                        .expirationTime(Date.from(claimExpiresAt));
                for (Map.Entry<String, Object> entry : claims.entrySet()) {
                    if (!entry.getKey().equals("sub")
                            && !entry.getKey().equals("iat")
                            && !entry.getKey().equals("exp")) {
                        claimsBuilder.claim(entry.getKey(), entry.getValue());
                    }
                }
                JWTClaimsSet claimsSet = claimsBuilder.build();
                JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                        .keyID("pagoflex-hs256")
                        .build();
                SignedJWT signedJwt = new SignedJWT(header, claimsSet);
                signedJwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
                String tokenValue = signedJwt.serialize();
                Map<String, Object> headers = Map.of("alg", "HS256", "typ", "JWT");
                Instant issuedAt = claimsSet.getIssueTime() == null
                        ? null : claimsSet.getIssueTime().toInstant();
                Instant expiresAt = claimsSet.getExpirationTime() == null
                        ? null : claimsSet.getExpirationTime().toInstant();
                return Jwt.withTokenValue(tokenValue)
                        .headers(h -> h.putAll(headers))
                        .claims(c -> c.putAll(claims))
                        .issuedAt(issuedAt)
                        .expiresAt(expiresAt)
                        .build();
            } catch (Exception ex) {
                throw new JwtEncodingException("No fue posible firmar el JWT", ex);
            }
        };
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(keySpec).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @PostConstruct
    void validarConfiguracion() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET es obligatorio");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes para HS256");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION_MS debe ser mayor que cero");
        }
    }

    public String generarToken(Long usuarioId, String correo, String rol) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plus(expirationMs, ChronoUnit.MILLIS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(correo)
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .claim("usuarioId", usuarioId)
                .claim("rol", rol)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decodificar(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException ex) {
            throw new TokenInvalidoException();
        }
    }

    @org.springframework.context.annotation.Bean
    public JwtDecoder jwtDecoder() {
        return jwtDecoder;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
