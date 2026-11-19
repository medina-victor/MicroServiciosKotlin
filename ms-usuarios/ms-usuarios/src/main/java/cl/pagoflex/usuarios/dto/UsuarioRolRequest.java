package cl.pagoflex.usuarios.dto;

import cl.pagoflex.usuarios.model.RolUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Solicitud administrativa para cambiar el rol. Esta operación aún no cuenta con autenticación.")
public class UsuarioRolRequest {

    @NotNull(message = "El rol es obligatorio")
    @Schema(description = "Nuevo rol del usuario", example = "EMPRESA", requiredMode = Schema.RequiredMode.REQUIRED)
    private RolUsuario rol;

    @Positive(message = "empresaId debe ser un número positivo")
    @Schema(description = "Identificador externo de empresa; obligatorio al asignar el rol EMPRESA", example = "15")
    private Long empresaId;

    public UsuarioRolRequest() {
    }

    public UsuarioRolRequest(RolUsuario rol, Long empresaId) {
        this.rol = rol;
        this.empresaId = empresaId;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}
