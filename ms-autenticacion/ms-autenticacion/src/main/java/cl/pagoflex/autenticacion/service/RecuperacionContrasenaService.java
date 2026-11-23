package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.dto.CambioContrasenaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.RespuestaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.SolicitudRecuperacionDTO;

public interface RecuperacionContrasenaService {
    RespuestaRecuperacionDTO solicitar(SolicitudRecuperacionDTO solicitud);

    RespuestaRecuperacionDTO cambiar(CambioContrasenaRecuperacionDTO solicitud);
}
