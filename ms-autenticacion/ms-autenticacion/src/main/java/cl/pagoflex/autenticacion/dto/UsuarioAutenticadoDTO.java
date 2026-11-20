package cl.pagoflex.autenticacion.dto;

public class UsuarioAutenticadoDTO {
    private Long usuarioId;
    private String correo;
    private String rol;

    public UsuarioAutenticadoDTO() {
    }

    public UsuarioAutenticadoDTO(Long usuarioId, String correo, String rol) {
        this.usuarioId = usuarioId;
        this.correo = correo;
        this.rol = rol;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
