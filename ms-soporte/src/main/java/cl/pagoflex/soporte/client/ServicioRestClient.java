package cl.pagoflex.soporte.client;

import cl.pagoflex.soporte.exception.DependenciaNoDisponibleException;
import cl.pagoflex.soporte.exception.DependenciaRemotaException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.function.Supplier;

abstract class ServicioRestClient {
    protected final RestClient restClient;
    private final String dependencia;

    protected ServicioRestClient(RestClient.Builder builder, String dependencia, String baseUrl) {
        this.dependencia = dependencia;
        this.restClient = builder.clone().baseUrl(baseUrl).build();
    }

    protected <T> T ejecutar(Supplier<T> request) {
        try {
            T response = request.get();
            if (response == null) throw new DependenciaNoDisponibleException(dependencia, "respuesta vacía");
            return response;
        } catch (HttpStatusCodeException ex) {
            throw new DependenciaRemotaException(dependencia, ex.getStatusCode().value());
        } catch (ResourceAccessException ex) {
            throw new DependenciaNoDisponibleException(dependencia, "sin respuesta dentro del tiempo límite");
        } catch (RestClientException ex) {
            throw new DependenciaNoDisponibleException(dependencia, "error de comunicación");
        }
    }
}
