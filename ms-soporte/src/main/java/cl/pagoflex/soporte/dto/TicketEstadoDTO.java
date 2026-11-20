package cl.pagoflex.soporte.dto;

import cl.pagoflex.soporte.model.EstadoTicket;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado del ticket")
public class TicketEstadoDTO {
    @NotNull(message = "El estado es obligatorio")
    private EstadoTicket estado;

    public TicketEstadoDTO() {
    }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
}
