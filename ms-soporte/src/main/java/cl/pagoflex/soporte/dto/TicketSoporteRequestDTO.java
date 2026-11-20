package cl.pagoflex.soporte.dto;

import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.PrioridadTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar o actualizar una solicitud de soporte")
public class TicketSoporteRequestDTO {
    @NotNull(message = "El identificador del usuario es obligatorio")
    @Positive(message = "El identificador del usuario debe ser mayor que cero")
    private Long usuarioId;

    @Positive(message = "El identificador de la empresa debe ser mayor que cero")
    private Long empresaId;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 150, message = "El asunto no puede superar 150 caracteres")
    private String asunto;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaSoporte categoria;

    private PrioridadTicket prioridad = PrioridadTicket.MEDIA;

    public TicketSoporteRequestDTO() {
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public CategoriaSoporte getCategoria() { return categoria; }
    public void setCategoria(CategoriaSoporte categoria) { this.categoria = categoria; }
    public PrioridadTicket getPrioridad() { return prioridad == null ? PrioridadTicket.MEDIA : prioridad; }
    public void setPrioridad(PrioridadTicket prioridad) { this.prioridad = prioridad; }
}
