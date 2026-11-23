package cl.pagoflex.gateway.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewaySecurityIntegrationTest {
    private static final String SECRET = "JWT_SECRET_FOR_TESTS_ONLY_0123456789";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void allowsLoginAndRegistrationWithoutJwt() {
        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(502)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

        webTestClient.post()
                .uri("/api/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isEqualTo(502);
    }

    @Test
    void allowsPasswordRecoveryEndpointsWithoutJwt() {
        webTestClient.post()
                .uri("/api/auth/recuperacion/solicitar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"correo\":\"usuario@example.com\"}")
                .exchange()
                .expectStatus().isEqualTo(502);

        webTestClient.post()
                .uri("/api/auth/recuperacion/cambiar")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"correo\":\"usuario@example.com\",\"codigo\":\"123456\",\"nuevaContrasena\":\"Nueva123\"}")
                .exchange()
                .expectStatus().isEqualTo(502);
    }

    @Test
    void rejectsProtectedRequestsWithoutJwt() {
        webTestClient.get()
                .uri("/api/compromisos/1")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401);
    }

    @Test
    void rejectsMalformedAndExpiredJwt() throws JOSEException {
        webTestClient.get()
                .uri("/api/compromisos/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.get()
                .uri("/api/compromisos/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CLIENTE", Instant.now().minusSeconds(60)))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void acceptsValidJwtAndEnforcesAdministrativeRoles() throws JOSEException {
        webTestClient.get()
                .uri("/api/compromisos/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CLIENTE", Instant.now().plusSeconds(300)))
                .exchange()
                .expectStatus().isEqualTo(502);

        webTestClient.get()
                .uri("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("CLIENTE", Instant.now().plusSeconds(300)))
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.status").isEqualTo(403);

        webTestClient.get()
                .uri("/api/usuarios")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("ADMIN", Instant.now().plusSeconds(300)))
                .exchange()
                .expectStatus().isEqualTo(502);
    }

    @Test
    void normalizesUnavailableServicesAndRejectsUnknownRoutes() {
        webTestClient.get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenUnchecked("CLIENTE", Instant.now().plusSeconds(300)))
                .exchange()
                .expectStatus().isEqualTo(502)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Servicio destino no disponible");

        webTestClient.get()
                .uri("/not-a-gateway-route")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(401);
    }

    @Test
    void allowsOnlyConfiguredCorsOrigins() {
        String allowedOrigin = "http://localhost:3000";
        org.springframework.web.cors.CorsConfiguration corsConfiguration =
                corsConfigurationSource.getCorsConfiguration(MockServerWebExchange.from(
                        MockServerHttpRequest.options("/api/pagos")
                                .header(HttpHeaders.ORIGIN, allowedOrigin)
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type")));
        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.checkOrigin(allowedOrigin)).isEqualTo(allowedOrigin);
        assertThat(corsConfiguration.checkHttpMethod(HttpMethod.POST)).isNotNull();
        assertThat(corsConfiguration.checkHeaders(java.util.List.of("authorization", "content-type"))).isNotNull();

        webTestClient.options()
                .uri("http://localhost:8090/api/pagos")
                .header(HttpHeaders.HOST, "localhost:8090")
                .header(HttpHeaders.ORIGIN, allowedOrigin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);

        webTestClient.options()
                .uri("http://localhost:8090/api/pagos")
                .header(HttpHeaders.HOST, "localhost:8090")
                .header(HttpHeaders.ORIGIN, "https://not-allowed.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    private String token(String role, Instant expiresAt) throws JOSEException {
        Instant issuedAt = Instant.now().minusSeconds(30);
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject("gateway-test@pagoflex.cl")
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("usuarioId", 1L)
                .claim("rol", role);
        if ("EMPRESA".equals(role)) {
            claims.claim("empresaId", 20L);
        }

        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        signedJwt.sign(new MACSigner(SECRET.getBytes(StandardCharsets.UTF_8)));
        return signedJwt.serialize();
    }

    private String tokenUnchecked(String role, Instant expiresAt) {
        try {
            return token(role, expiresAt);
        } catch (JOSEException ex) {
            throw new IllegalStateException("No se pudo crear el JWT de prueba", ex);
        }
    }
}
