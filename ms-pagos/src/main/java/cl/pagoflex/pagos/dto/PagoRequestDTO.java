package cl.pagoflex.pagos.dto;

import cl.pagoflex.pagos.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos necesarios para registrar un pago simulado")
public class PagoRequestDTO {

    @NotNull(message = "El identificador del compromiso es obligatorio")
    @Positive(message = "El identificador del compromiso debe ser positivo")
    @Schema(example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long compromisoId;

    @NotNull(message = "El identificador del usuario es obligatorio")
    @Positive(message = "El identificador del usuario debe ser positivo")
    @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long usuarioId;

    @NotNull(message = "El identificador de la empresa es obligatorio")
    @Positive(message = "El identificador de la empresa debe ser positivo")
    @Schema(example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long empresaId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero")
    @Schema(example = "150000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal monto;

    @NotNull(message = "El método de pago es obligatorio")
    @Schema(example = "TRANSFERENCIA", requiredMode = Schema.RequiredMode.REQUIRED)
    private MetodoPago metodoPago;

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(example = "Pago de cuota mensual")
    private String descripcion;

    public PagoRequestDTO() {
    }

    public Long getCompromisoId() { return compromisoId; }
    public void setCompromisoId(Long compromisoId) { this.compromisoId = compromisoId; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
