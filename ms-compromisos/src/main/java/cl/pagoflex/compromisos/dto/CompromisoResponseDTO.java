package cl.pagoflex.compromisos.dto;

import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CompromisoResponseDTO {
    private Long id;
    private Long usuarioId;
    private Long empresaId;
    private String descripcion;
    private BigDecimal monto;
    private LocalDateTime fechaRegistro;
    private LocalDate fechaVencimiento;
    private EstadoCompromiso estado;
    private String observaciones;

    public CompromisoResponseDTO() {
    }

    public CompromisoResponseDTO(Long id, Long usuarioId, Long empresaId, String descripcion,
                                 BigDecimal monto, LocalDateTime fechaRegistro,
                                 LocalDate fechaVencimiento, EstadoCompromiso estado,
                                 String observaciones) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fechaRegistro = fechaRegistro;
        this.fechaVencimiento = fechaVencimiento;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public static CompromisoResponseDTO desde(Compromiso compromiso) {
        return new CompromisoResponseDTO(compromiso.getId(), compromiso.getUsuarioId(),
                compromiso.getEmpresaId(), compromiso.getDescripcion(), compromiso.getMonto(),
                compromiso.getFechaRegistro(), compromiso.getFechaVencimiento(),
                compromiso.getEstado(), compromiso.getObservaciones());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public EstadoCompromiso getEstado() { return estado; }
    public void setEstado(EstadoCompromiso estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
