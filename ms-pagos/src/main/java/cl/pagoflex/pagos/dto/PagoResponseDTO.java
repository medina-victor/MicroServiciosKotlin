package cl.pagoflex.pagos.dto;

import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.MetodoPago;
import cl.pagoflex.pagos.model.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PagoResponseDTO {
    private Long id;
    private Long compromisoId;
    private Long usuarioId;
    private Long empresaId;
    private BigDecimal monto;
    private LocalDateTime fechaPago;
    private MetodoPago metodoPago;
    private EstadoPago estado;
    private String referenciaTransaccion;
    private String descripcion;

    public PagoResponseDTO() {
    }

    public PagoResponseDTO(Long id, Long compromisoId, Long usuarioId, Long empresaId, BigDecimal monto,
                           LocalDateTime fechaPago, MetodoPago metodoPago, EstadoPago estado,
                           String referenciaTransaccion, String descripcion) {
        this.id = id;
        this.compromisoId = compromisoId;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.monto = monto;
        this.fechaPago = fechaPago;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.referenciaTransaccion = referenciaTransaccion;
        this.descripcion = descripcion;
    }

    public static PagoResponseDTO desde(Pago pago) {
        return new PagoResponseDTO(pago.getId(), pago.getCompromisoId(), pago.getUsuarioId(), pago.getEmpresaId(),
                pago.getMonto(), pago.getFechaPago(), pago.getMetodoPago(), pago.getEstado(),
                pago.getReferenciaTransaccion(), pago.getDescripcion());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompromisoId() { return compromisoId; }
    public void setCompromisoId(Long compromisoId) { this.compromisoId = compromisoId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public EstadoPago getEstado() { return estado; }
    public void setEstado(EstadoPago estado) { this.estado = estado; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) {
        this.referenciaTransaccion = referenciaTransaccion;
    }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
