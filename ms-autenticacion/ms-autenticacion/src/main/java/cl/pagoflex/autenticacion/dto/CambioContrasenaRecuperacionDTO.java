package cl.pagoflex.autenticacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Cambio de contraseña mediante codigo de recuperacion")
public class CambioContrasenaRecuperacionDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 254, message = "El correo no puede superar 254 caracteres")
    private String correo;

    @NotBlank(message = "El codigo de recuperacion es obligatorio")
    @Size(min = 6, max = 32, message = "El codigo debe contener entre 6 y 32 caracteres")
    private String codigo;

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$",
            message = "La contraseña debe incluir al menos una letra y un numero")
    private String nuevaContrasena;

    public CambioContrasenaRecuperacionDTO() {}

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }
}
