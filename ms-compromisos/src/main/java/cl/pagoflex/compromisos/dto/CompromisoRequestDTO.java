package cl.pagoflex.compromisos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Datos para registrar o actualizar un compromiso")
public class CompromisoRequestDTO {

    @NotNull(message = "El identificador del usuario es obligatorio")
    @Positive(message = "El identificador del usuario debe ser positivo")
    @Schema(example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long usuarioId;

    @NotNull(message = "El identificador de la empresa es obligatorio")
    @Positive(message = "El identificador de la empresa debe ser positivo")
    @Schema(example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long empresaId;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
    @Schema(example = "Cuota mensual de servicio", requiredMode = Schema.RequiredMode.REQUIRED)
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor que cero")
    @Schema(example = "150000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal monto;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Schema(example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaVencimiento;

    @Size(max = 1000, message = "Las observaciones no pueden superar 1000 caracteres")
    @Schema(example = "Pago correspondiente al mes")
    private String observaciones;

    public CompromisoRequestDTO() {
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
