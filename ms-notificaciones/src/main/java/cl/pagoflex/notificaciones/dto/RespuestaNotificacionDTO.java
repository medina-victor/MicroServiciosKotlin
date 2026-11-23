package cl.pagoflex.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RespuestaNotificacion")
public record RespuestaNotificacionDTO(
        @Schema(description = "Indica si el correo fue aceptado para envio", example = "true")
        boolean enviado,
        @Schema(description = "Mensaje de confirmacion", example = "Correo enviado correctamente")
        String mensaje
) {
}
