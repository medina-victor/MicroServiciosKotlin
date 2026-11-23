package cl.pagoflex.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SolicitudCambioContrasena")
public record SolicitudCambioContrasenaDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        @Schema(description = "Correo del destinatario", example = "usuario@example.com")
        String correo
) {
}
