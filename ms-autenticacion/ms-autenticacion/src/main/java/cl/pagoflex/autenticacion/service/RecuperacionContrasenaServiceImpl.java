package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.client.NotificacionesCliente;
import cl.pagoflex.autenticacion.dto.CambioContrasenaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.RespuestaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.SolicitudCambioCorreoNotificacionesDTO;
import cl.pagoflex.autenticacion.dto.SolicitudCorreoNotificacionesDTO;
import cl.pagoflex.autenticacion.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.autenticacion.exception.CodigoRecuperacionInvalidoException;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class RecuperacionContrasenaServiceImpl implements RecuperacionContrasenaService {
    private static final String MENSAJE_SOLICITUD =
            "Si el correo existe, recibira instrucciones de recuperacion";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CredencialRepository credencialRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificacionesCliente notificacionesCliente;

    public RecuperacionContrasenaServiceImpl(CredencialRepository credencialRepository,
                                             PasswordEncoder passwordEncoder,
                                             NotificacionesCliente notificacionesCliente) {
        this.credencialRepository = credencialRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificacionesCliente = notificacionesCliente;
    }

    @Override
    public RespuestaRecuperacionDTO solicitar(SolicitudRecuperacionDTO solicitud) {
        String correo = solicitud.getCorreo().trim().toLowerCase();
        Credencial credencial = credencialRepository.findByCorreoForUpdate(correo).orElse(null);
        if (credencial == null || !credencial.isActivo()) {
            return new RespuestaRecuperacionDTO(MENSAJE_SOLICITUD);
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (credencial.getRecoveryRequestedAt() != null
                && credencial.getRecoveryRequestedAt().plusSeconds(60).isAfter(ahora)) {
            return new RespuestaRecuperacionDTO(MENSAJE_SOLICITUD);
        }

        credencial.setRecoveryCodeUsed(true);
        credencial.setRecoveryCodeHash(null);
        credencial.setRecoveryCodeExpiration(null);
        credencial.setRecoveryAttempts(0);
        credencial.setRecoveryLockedUntil(null);
        credencial.setRecoveryRequestedAt(ahora);

        String codigo = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        credencial.setRecoveryCodeHash(hash(codigo));
        credencial.setRecoveryCodeExpiration(ahora.plusMinutes(10));
        credencial.setRecoveryCodeUsed(false);
        credencialRepository.save(credencial);

        try {
            notificacionesCliente.enviarCodigoRecuperacion(
                    new SolicitudCorreoNotificacionesDTO(credencial.getCorreo(), codigo));
        } catch (RuntimeException exception) {
            credencial.setRecoveryCodeUsed(true);
            credencial.setRecoveryCodeHash(null);
            credencial.setRecoveryCodeExpiration(null);
            credencialRepository.save(credencial);
            throw exception;
        }

        return new RespuestaRecuperacionDTO(MENSAJE_SOLICITUD);
    }

    @Override
    @Transactional(noRollbackFor = CodigoRecuperacionInvalidoException.class)
    public RespuestaRecuperacionDTO cambiar(CambioContrasenaRecuperacionDTO solicitud) {
        String correo = solicitud.getCorreo().trim().toLowerCase();
        Credencial credencial = credencialRepository.findByCorreoForUpdate(correo).orElse(null);
        if (credencial == null || !credencial.isActivo()) {
            throw new CodigoRecuperacionInvalidoException();
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (credencial.getRecoveryLockedUntil() != null
                && credencial.getRecoveryLockedUntil().isAfter(ahora)) {
            throw new CodigoRecuperacionInvalidoException();
        }
        if (credencial.isRecoveryCodeUsed()
                || credencial.getRecoveryCodeHash() == null
                || credencial.getRecoveryCodeExpiration() == null
                || credencial.getRecoveryCodeExpiration().isBefore(ahora)) {
            throw new CodigoRecuperacionInvalidoException();
        }

        int intentos = credencial.getRecoveryAttempts() + 1;
        credencial.setRecoveryAttempts(intentos);
        if (intentos >= 5) {
            credencial.setRecoveryLockedUntil(ahora.plusMinutes(15));
            credencialRepository.save(credencial);
            throw new CodigoRecuperacionInvalidoException();
        }

        if (!MessageDigest.isEqual(
                credencial.getRecoveryCodeHash().getBytes(StandardCharsets.UTF_8),
                hash(solicitud.getCodigo()).getBytes(StandardCharsets.UTF_8))) {
            credencialRepository.save(credencial);
            throw new CodigoRecuperacionInvalidoException();
        }

        credencial.setPasswordHash(passwordEncoder.encode(solicitud.getNuevaContrasena()));
        credencial.setRecoveryCodeUsed(true);
        credencial.setRecoveryCodeHash(null);
        credencial.setRecoveryCodeExpiration(null);
        credencial.setRecoveryAttempts(0);
        credencial.setRecoveryLockedUntil(null);
        credencialRepository.save(credencial);

        notificacionesCliente.enviarConfirmacionCambio(
                new SolicitudCambioCorreoNotificacionesDTO(credencial.getCorreo()));

        return new RespuestaRecuperacionDTO("Contraseña modificada correctamente");
    }

    private String hash(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(codigo.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder();
            for (byte value : bytes) {
                hexadecimal.append(String.format("%02x", value));
            }
            return hexadecimal.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("No fue posible proteger el codigo", exception);
        }
    }
}
