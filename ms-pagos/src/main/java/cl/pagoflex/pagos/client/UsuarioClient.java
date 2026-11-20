package cl.pagoflex.pagos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UsuarioClient extends ServicioRestClient {
    public UsuarioClient(RestClient.Builder builder,
                         @Value("${services.usuarios.url:http://ms-usuarios}") String baseUrl) {
        super(builder, "ms-usuarios", baseUrl);
    }

    public UsuarioExterno buscarPorId(Long id) {
        UsuarioExterno usuario = ejecutar(() -> restClient.get().uri("/api/usuarios/{id}", id)
                .retrieve().body(UsuarioExterno.class));
        validarId(id, usuario.id());
        return usuario;
    }

    public record UsuarioExterno(Long id, Boolean activo) {
    }
}
