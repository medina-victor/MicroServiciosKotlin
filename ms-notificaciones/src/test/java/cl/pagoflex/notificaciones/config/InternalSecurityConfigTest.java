package cl.pagoflex.notificaciones.config;

import cl.pagoflex.notificaciones.security.ServiceTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InternalSecurityConfigTest {
    private static final String TOKEN = "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789";

    @Test
    void rechazaSolicitudSinToken() throws Exception {
        ServiceTokenFilter filter = new ServiceTokenFilter(TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/notificaciones/recuperacion");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (jakarta.servlet.ServletRequest req,
                                          jakarta.servlet.ServletResponse res) -> { });

        assertEquals(401, response.getStatus());
    }

    @Test
    void rechazaTokenIncorrecto() throws Exception {
        ServiceTokenFilter filter = new ServiceTokenFilter(TOKEN);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/notificaciones/recuperacion");
        request.addHeader("X-Internal-Token", "INCORRECTO_PERO_SUFICIENTEMENTE_LARGO_123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (jakarta.servlet.ServletRequest req,
                                          jakarta.servlet.ServletResponse res) -> { });

        assertEquals(403, response.getStatus());
    }

    @Test
    void exigeTokenDeAlMenos32Bytes() {
        assertThrows(IllegalStateException.class,
                () -> new ServiceTokenFilter("corto"));
    }
}
