package cl.pagoflex.usuarios.dto;

import cl.pagoflex.usuarios.model.RolUsuario;
import cl.pagoflex.usuarios.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información pública de un usuario")
public class UsuarioResponse {

    @Schema(description = "Identificador autogenerado", example = "1")
    private Long id;

    @Schema(description = "Nombre completo", example = "Juan Perez")
    private String nombre;

    @Schema(description = "Correo único", example = "juan@demo.cl")
    private String correo;

    @Schema(description = "Teléfono de contacto", example = "912345678")
    private String telefono;

    @Schema(description = "Rol asignado al usuario", example = "CLIENTE")
    private RolUsuario rol;

    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean activo;

    @Schema(description = "Fecha y hora de registro")
    private LocalDateTime fechaRegistro;

    @Schema(description = "Identificador externo de empresa, cuando corresponda", example = "15")
    private Long empresaId;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Long id, String nombre, String correo, String telefono, RolUsuario rol,
                           Boolean activo, LocalDateTime fechaRegistro, Long empresaId) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.empresaId = empresaId;
    }

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo(),
                usuario.getTelefono(), usuario.getRol(), usuario.getActivo(),
                usuario.getFechaRegistro(), usuario.getEmpresaId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
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

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }
}
