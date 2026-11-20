package cl.pagoflex.pagos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EmpresaClient extends ServicioRestClient {
    public EmpresaClient(RestClient.Builder builder,
                         @Value("${services.empresas.url:http://ms-empresas}") String baseUrl) {
        super(builder, "ms-empresas", baseUrl);
    }

    public EmpresaExterna buscarPorId(Long id) {
        EmpresaExterna empresa = ejecutar(() -> restClient.get().uri("/api/empresas/{id}", id)
                .retrieve().body(EmpresaExterna.class));
        validarId(id, empresa.id());
        return empresa;
    }

    public record EmpresaExterna(Long id, Boolean activo) {
    }
}
