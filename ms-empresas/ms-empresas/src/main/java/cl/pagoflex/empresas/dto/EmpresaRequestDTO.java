package cl.pagoflex.empresas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar o actualizar una empresa")
public class EmpresaRequestDTO {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede superar 150 caracteres")
    @Schema(description = "Razón social", example = "Comercial Demo SpA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String razonSocial;

    @NotBlank(message = "El nombre comercial es obligatorio")
    @Size(max = 100, message = "El nombre comercial no puede superar 100 caracteres")
    @Schema(description = "Nombre comercial", example = "Demo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreComercial;

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12, message = "El RUT no puede superar 12 caracteres")
    @Pattern(regexp = "^(?:[0-9]{7,8}|[0-9]{1,2}(?:\\.[0-9]{3}){2})-?[0-9kK]$",
            message = "El RUT debe tener un formato válido, por ejemplo 76.123.456-7")
    @Schema(description = "RUT único de la empresa", example = "76.123.456-7", requiredMode = Schema.RequiredMode.REQUIRED)
    private String rut;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 255, message = "El correo no puede superar 255 caracteres")
    @Schema(description = "Correo de contacto", example = "contacto@demo.cl", requiredMode = Schema.RequiredMode.REQUIRED)
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 30, message = "El teléfono no puede superar 30 caracteres")
    @Schema(description = "Teléfono de contacto", example = "+56 9 1234 5678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres")
    @Schema(description = "Dirección de la empresa", example = "Av. Providencia 123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccion;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    @Schema(description = "Descripción opcional", example = "Empresa de demostración")
    private String descripcion;

    public EmpresaRequestDTO() {
    }

    public EmpresaRequestDTO(String razonSocial, String nombreComercial, String rut, String correo,
                             String telefono, String direccion, String descripcion) {
        this.razonSocial = razonSocial;
        this.nombreComercial = nombreComercial;
        this.rut = rut;
        this.correo = correo;
        this.telefono = telefono;
        this.direccion = direccion;
        this.descripcion = descripcion;
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
}
