package cl.pagoflex.compromisos.client;

import cl.pagoflex.compromisos.exception.DependenciaNoDisponibleException;
import cl.pagoflex.compromisos.exception.DependenciaRemotaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class EmpresaClient {
    private final RestClient restClient;

    public EmpresaClient(RestClient.Builder builder, @Value("${services.empresas.url:http://ms-empresas}") String baseUrl) {
        this.restClient = builder.clone().baseUrl(baseUrl).build();
    }

    public EmpresaExterna buscarPorId(Long id) {
        try {
            EmpresaExterna empresa = restClient.get().uri("/api/empresas/{id}", id)
                    .retrieve().body(EmpresaExterna.class);
            if (empresa == null) throw new DependenciaNoDisponibleException("ms-empresas", "respuesta vacía");
            if (!id.equals(empresa.id())) {
                throw new DependenciaNoDisponibleException("ms-empresas", "respuesta con identificador inconsistente");
            }
            return empresa;
        } catch (HttpStatusCodeException ex) {
            throw new DependenciaRemotaException("ms-empresas", ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new DependenciaNoDisponibleException("ms-empresas", "sin respuesta dentro del tiempo límite");
        } catch (RestClientException ex) {
            throw new DependenciaNoDisponibleException("ms-empresas", "error de comunicación");
        }
    }

    public record EmpresaExterna(Long id, Boolean activo) {
    }
}
