package cl.pagoflex.autenticacion.client;

import cl.pagoflex.autenticacion.exception.ServicioUsuariosNoDisponibleException;
import cl.pagoflex.autenticacion.exception.UsuarioNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioCliente {

    private final RestClient restClient;

    public UsuarioCliente(@Value("${usuarios.service.url}") String usuariosUrl) {
        this.restClient = RestClient.builder().baseUrl(usuariosUrl).build();
    }

    public UsuarioExterno obtenerPorId(Long usuarioId) {
        return obtener("/api/usuarios/" + usuarioId);
    }

    public UsuarioExterno obtenerPorCorreo(String correo) {
        return obtener("/api/usuarios/correo/" + correo);
    }

    private UsuarioExterno obtener(String ruta) {
        try {
            return restClient.get().uri(ruta).retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new UsuarioNoEncontradoException();
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ServicioUsuariosNoDisponibleException();
                    })
                    .body(UsuarioExterno.class);
        } catch (UsuarioNoEncontradoException | ServicioUsuariosNoDisponibleException ex) {
            throw ex;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new UsuarioNoEncontradoException();
        } catch (ResourceAccessException ex) {
            throw new ServicioUsuariosNoDisponibleException();
        } catch (RuntimeException ex) {
            throw new ServicioUsuariosNoDisponibleException();
        }
    }

    public record UsuarioExterno(Long id, String correo, String rol, Boolean activo) {
    }
}
