package cl.pagoflex.notificaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "SolicitudRecuperacion")
public record SolicitudRecuperacionDTO(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato valido")
        @Size(max = 254, message = "El correo no puede superar 254 caracteres")
        @Schema(description = "Correo del destinatario", example = "usuario@example.com")
        String correo,

        @NotBlank(message = "El codigo de recuperacion es obligatorio")
        @Size(min = 1, max = 32, message = "El codigo debe contener entre 1 y 32 caracteres")
        @Schema(description = "Codigo temporal generado por ms-autenticacion", example = "123456")
        String codigo
) {
}
