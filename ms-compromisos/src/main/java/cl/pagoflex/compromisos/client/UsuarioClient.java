package cl.pagoflex.compromisos.client;

import cl.pagoflex.compromisos.exception.DependenciaNoDisponibleException;
import cl.pagoflex.compromisos.exception.DependenciaRemotaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UsuarioClient {
    private final RestClient restClient;

    public UsuarioClient(RestClient.Builder builder, @Value("${services.usuarios.url:http://ms-usuarios}") String baseUrl) {
        this.restClient = builder.clone().baseUrl(baseUrl).build();
    }

    public UsuarioExterno buscarPorId(Long id) {
        try {
            UsuarioExterno usuario = restClient.get().uri("/api/usuarios/{id}", id)
                    .retrieve().body(UsuarioExterno.class);
            if (usuario == null) throw new DependenciaNoDisponibleException("ms-usuarios", "respuesta vacía");
            if (!id.equals(usuario.id())) {
                throw new DependenciaNoDisponibleException("ms-usuarios", "respuesta con identificador inconsistente");
            }
            return usuario;
        } catch (HttpStatusCodeException ex) {
            throw new DependenciaRemotaException("ms-usuarios", ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new DependenciaNoDisponibleException("ms-usuarios", "sin respuesta dentro del tiempo límite");
        } catch (RestClientException ex) {
            throw new DependenciaNoDisponibleException("ms-usuarios", "error de comunicación");
        }
    }

    public record UsuarioExterno(Long id, Boolean activo) {
    }
}
