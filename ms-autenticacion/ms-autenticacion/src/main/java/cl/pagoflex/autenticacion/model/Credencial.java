package cl.pagoflex.autenticacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "credenciales")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private Long usuarioId;

    @Column(nullable = false, unique = true, length = 255)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "recovery_code_hash", length = 64)
    private String recoveryCodeHash;

    @Column(name = "recovery_code_expiration")
    private LocalDateTime recoveryCodeExpiration;

    @Column(name = "recovery_code_used", nullable = false)
    private boolean recoveryCodeUsed = true;

    @Column(name = "recovery_attempts", nullable = false)
    private int recoveryAttempts = 0;

    @Column(name = "recovery_locked_until")
    private LocalDateTime recoveryLockedUntil;

    @Column(name = "recovery_requested_at")
    private LocalDateTime recoveryRequestedAt;

    public Credencial() {
    }

    @PrePersist
    void alCrear() {
        LocalDateTime ahora = LocalDateTime.now();
        fechaCreacion = ahora;
        fechaActualizacion = ahora;
    }

    @PreUpdate
    void alActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public String getRecoveryCodeHash() { return recoveryCodeHash; }
    public void setRecoveryCodeHash(String recoveryCodeHash) { this.recoveryCodeHash = recoveryCodeHash; }
    public LocalDateTime getRecoveryCodeExpiration() { return recoveryCodeExpiration; }
    public void setRecoveryCodeExpiration(LocalDateTime recoveryCodeExpiration) { this.recoveryCodeExpiration = recoveryCodeExpiration; }
    public boolean isRecoveryCodeUsed() { return recoveryCodeUsed; }
    public void setRecoveryCodeUsed(boolean recoveryCodeUsed) { this.recoveryCodeUsed = recoveryCodeUsed; }
    public int getRecoveryAttempts() { return recoveryAttempts; }
    public void setRecoveryAttempts(int recoveryAttempts) { this.recoveryAttempts = recoveryAttempts; }
    public LocalDateTime getRecoveryLockedUntil() { return recoveryLockedUntil; }
    public void setRecoveryLockedUntil(LocalDateTime recoveryLockedUntil) { this.recoveryLockedUntil = recoveryLockedUntil; }
    public LocalDateTime getRecoveryRequestedAt() { return recoveryRequestedAt; }
    public void setRecoveryRequestedAt(LocalDateTime recoveryRequestedAt) { this.recoveryRequestedAt = recoveryRequestedAt; }
}
