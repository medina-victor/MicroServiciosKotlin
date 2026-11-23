package cl.pagoflex.notificaciones.service;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;

public interface NotificacionService {
    RespuestaNotificacionDTO enviarCodigoRecuperacion(SolicitudRecuperacionDTO solicitud);

    RespuestaNotificacionDTO enviarConfirmacionCambioContrasena(SolicitudCambioContrasenaDTO solicitud);
}
