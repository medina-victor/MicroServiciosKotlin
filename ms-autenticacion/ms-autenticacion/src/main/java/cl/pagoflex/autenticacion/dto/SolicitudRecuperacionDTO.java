package cl.pagoflex.autenticacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de recuperacion de contraseña")
public class SolicitudRecuperacionDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres")
    private String correo;

    public SolicitudRecuperacionDTO() {}

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
