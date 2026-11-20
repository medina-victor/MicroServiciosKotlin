package cl.pagoflex.soporte.dto;

import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.model.TicketSoporte;

import java.time.LocalDateTime;

public class TicketSoporteResponseDTO {
    private Long id;
    private Long usuarioId;
    private Long empresaId;
    private String asunto;
    private String descripcion;
    private CategoriaSoporte categoria;
    private PrioridadTicket prioridad;
    private EstadoTicket estado;
    private String respuesta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private LocalDateTime fechaCierre;

    public TicketSoporteResponseDTO() {
    }

    public static TicketSoporteResponseDTO desde(TicketSoporte ticket) {
        TicketSoporteResponseDTO dto = new TicketSoporteResponseDTO();
        dto.id = ticket.getId();
        dto.usuarioId = ticket.getUsuarioId();
        dto.empresaId = ticket.getEmpresaId();
        dto.asunto = ticket.getAsunto();
        dto.descripcion = ticket.getDescripcion();
        dto.categoria = ticket.getCategoria();
        dto.prioridad = ticket.getPrioridad();
        dto.estado = ticket.getEstado();
        dto.respuesta = ticket.getRespuesta();
        dto.fechaCreacion = ticket.getFechaCreacion();
        dto.fechaActualizacion = ticket.getFechaActualizacion();
        dto.fechaCierre = ticket.getFechaCierre();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public PrioridadTicket getPrioridad() { return prioridad; }
    public void setPrioridad(PrioridadTicket prioridad) { this.prioridad = prioridad; }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
}
