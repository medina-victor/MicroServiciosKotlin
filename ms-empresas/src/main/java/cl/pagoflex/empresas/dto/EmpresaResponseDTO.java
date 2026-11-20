package cl.pagoflex.empresas.dto;

import cl.pagoflex.empresas.model.Empresa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de una empresa")
public class EmpresaResponseDTO {

    @Schema(description = "Identificador autogenerado", example = "1")
    private Long id;

    @Schema(example = "Comercial Demo SpA")
    private String razonSocial;

    @Schema(example = "Demo")
    private String nombreComercial;

    @Schema(example = "76.123.456-7")
    private String rut;

    @Schema(example = "contacto@demo.cl")
    private String correo;

    @Schema(example = "+56 9 1234 5678")
    private String telefono;

    @Schema(example = "Av. Providencia 123")
    private String direccion;

    @Schema(example = "Empresa de demostración")
    private String descripcion;

    @Schema(example = "true")
    private Boolean activo;

    @Schema(description = "Fecha y hora de registro")
    private LocalDateTime fechaRegistro;

    public EmpresaResponseDTO() {
    }

    public EmpresaResponseDTO(Long id, String razonSocial, String nombreComercial, String rut,
                              String correo, String telefono, String direccion, String descripcion,
                              Boolean activo, LocalDateTime fechaRegistro) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
        this.rut = rut;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    public static EmpresaResponseDTO desde(Empresa empresa) {
        return new EmpresaResponseDTO(empresa.getId(), empresa.getRazonSocial(),
                empresa.getNombreComercial(), empresa.getRut(), empresa.getCorreo(),
                empresa.getTelefono(), empresa.getDireccion(), empresa.getDescripcion(),
                empresa.getActivo(), empresa.getFechaRegistro());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
