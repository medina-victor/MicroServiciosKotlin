package cl.pagoflex.compromisos.dto;

import cl.pagoflex.compromisos.model.EstadoCompromiso;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO auxiliar para representar el estado de un compromiso")
public class CompromisoEstadoDTO {
    @Schema(example = "PAGADO")
    private EstadoCompromiso estado;

    public CompromisoEstadoDTO() {
    }

    public CompromisoEstadoDTO(EstadoCompromiso estado) {
        this.estado = estado;
    }

    public EstadoCompromiso getEstado() { return estado; }
    public void setEstado(EstadoCompromiso estado) { this.estado = estado; }
}
