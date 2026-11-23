package cl.pagoflex.notificaciones.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
public class ServiceTokenFilter extends OncePerRequestFilter {
    private final String expectedToken;

    public ServiceTokenFilter(@Value("${services.internal-token:}") String expectedToken) {
        if (expectedToken == null || expectedToken.isBlank()) {
            throw new IllegalStateException(
                    "INTERNAL_SERVICE_TOKEN es obligatorio; configúralo en el entorno con el mismo valor en todos los servicios");
        }
        if (expectedToken.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "INTERNAL_SERVICE_TOKEN debe tener al menos 32 bytes y el mismo valor en todos los servicios");
        }
        this.expectedToken = expectedToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || "/swagger-ui.html".equals(path)
                || "/actuator/health".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String suppliedToken = request.getHeader("X-Internal-Token");
        if (suppliedToken == null || suppliedToken.isBlank()) {
            responder(response, HttpServletResponse.SC_UNAUTHORIZED, "Falta el token interno");
            return;
        }
        if (!MessageDigest.isEqual(expectedToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            responder(response, HttpServletResponse.SC_FORBIDDEN, "Token interno invalido");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("service", null,
                        List.of(new SimpleGrantedAuthority("ROLE_SERVICE"))));
        chain.doFilter(request, response);
    }

    private void responder(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"estado\":" + status + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
