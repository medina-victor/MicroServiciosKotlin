package cl.pagoflex.autenticacion.client;

import cl.pagoflex.autenticacion.dto.SolicitudCambioCorreoNotificacionesDTO;
import cl.pagoflex.autenticacion.dto.SolicitudCorreoNotificacionesDTO;
import cl.pagoflex.autenticacion.exception.NotificacionesNoDisponibleException;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NotificacionesCliente {
    private final RestClient restClient;

    public NotificacionesCliente(@LoadBalanced RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void enviarCodigoRecuperacion(SolicitudCorreoNotificacionesDTO solicitud) {
        enviar("/api/notificaciones/recuperacion", solicitud);
    }

    public void enviarConfirmacionCambio(SolicitudCambioCorreoNotificacionesDTO solicitud) {
        enviar("/api/notificaciones/cambio-contrasena", solicitud);
    }

    private void enviar(String ruta, Object solicitud) {
        try {
            restClient.post()
                    .uri("http://MS-NOTIFICACIONES" + ruta)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(solicitud)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new NotificacionesNoDisponibleException();
        }
    }
}
