package cl.pagoflex.autenticacion.dto;

public class AuthResponseDTO {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long usuarioId;
    private Long empresaId;
    private String correo;
    private String rol;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String accessToken, String tokenType, long expiresIn,
                           Long usuarioId, String correo, String rol) {
        this(accessToken, tokenType, expiresIn, usuarioId, null, correo, rol);
    }

    public AuthResponseDTO(String accessToken, String tokenType, long expiresIn,
                           Long usuarioId, Long empresaId, String correo, String rol) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.usuarioId = usuarioId;
        this.empresaId = empresaId;
        this.correo = correo;
        this.rol = rol;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
