package cl.pagoflex.soporte.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Respuesta de soporte")
public class TicketRespuestaDTO {
    @NotBlank(message = "La respuesta es obligatoria")
    @Size(max = 2000, message = "La respuesta no puede superar 2000 caracteres")
    private String respuesta;

    public TicketRespuestaDTO() {
    }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
}
