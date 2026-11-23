package cl.pagoflex.autenticacion.security;

import cl.pagoflex.autenticacion.exception.TokenInvalidoException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(
            "0123456789abcdef0123456789abcdef", 3600000L);

    @Test
    void generaFirmaValidaConClaimsEsperados() {
        String token = jwtService.generarToken(42L, "admin@pagoflex.cl", "ADMIN");
        Jwt jwt = jwtService.decodificar(token);

        assertThat(jwt.getSubject()).isEqualTo("admin@pagoflex.cl");
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("ADMIN");
        assertThat(((Number) jwt.getClaim("usuarioId")).longValue()).isEqualTo(42L);
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void rechazaFirmaInvalida() {
        String token = jwtService.generarToken(42L, "admin@pagoflex.cl", "ADMIN");
        String alterado = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.decodificar(alterado))
                .isInstanceOf(TokenInvalidoException.class);
    }

    @Test
    void rechazaConfiguracionInsegura() {
        assertThatThrownBy(() -> new JwtService("corto", 3600000L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void incluyeEmpresaEnLosClaimsCuandoCorresponde() {
        String token = jwtService.generarToken(42L, "empresa@pagoflex.cl", "EMPRESA", 20L);
        Jwt jwt = jwtService.decodificar(token);

        assertThat(((Number) jwt.getClaim("empresaId")).longValue()).isEqualTo(20L);
    }
}
