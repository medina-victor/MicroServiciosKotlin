package cl.pagoflex.autenticacion.client;

import cl.pagoflex.autenticacion.exception.ServicioUsuariosNoDisponibleException;
import cl.pagoflex.autenticacion.exception.RespuestaServicioUsuariosException;
import cl.pagoflex.autenticacion.exception.UsuarioNoEncontradoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UsuarioCliente {

    private final RestClient restClient;

    public UsuarioCliente(RestClient.Builder builder,
                          @Value("${usuarios.service.url:http://ms-usuarios}") String usuariosUrl) {
        this.restClient = builder.clone().baseUrl(usuariosUrl).build();
    }

    public UsuarioExterno obtenerPorId(Long usuarioId) {
        UsuarioExterno usuario = obtener("/api/usuarios/" + usuarioId);
        if (usuario.id() == null || !usuarioId.equals(usuario.id())) {
            throw new ServicioUsuariosNoDisponibleException("ms-usuarios devolvió un identificador inconsistente");
        }
        return usuario;
    }

    public UsuarioExterno obtenerPorCorreo(String correo) {
        return obtener("/api/usuarios/correo/" + correo);
    }

    private UsuarioExterno obtener(String ruta) {
        try {
            return restClient.get().uri(ruta).retrieve()
                    .onStatus(status -> status.value() == 404, (request, response) -> {
                        throw new UsuarioNoEncontradoException();
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new RespuestaServicioUsuariosException(response.getStatusCode().value());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new RespuestaServicioUsuariosException(response.getStatusCode().value());
                    })
                    .body(UsuarioExterno.class);
        } catch (UsuarioNoEncontradoException | ServicioUsuariosNoDisponibleException
                 | RespuestaServicioUsuariosException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw new ServicioUsuariosNoDisponibleException("ms-usuarios no respondió dentro del tiempo límite");
        } catch (RestClientException ex) {
            throw new ServicioUsuariosNoDisponibleException("Error de comunicación con ms-usuarios");
        }
    }

    public record UsuarioExterno(Long id, String correo, String rol, Boolean activo, Long empresaId) {
        public UsuarioExterno(Long id, String correo, String rol, Boolean activo) {
            this(id, correo, rol, activo, null);
        }
    }
}
