package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.client.NotificacionesCliente;
import cl.pagoflex.autenticacion.dto.CambioContrasenaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.SolicitudCorreoNotificacionesDTO;
import cl.pagoflex.autenticacion.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.autenticacion.exception.CodigoRecuperacionInvalidoException;
import cl.pagoflex.autenticacion.exception.NotificacionesNoDisponibleException;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaServiceTest {
    @Mock
    private CredencialRepository repository;
    @Mock
    private NotificacionesCliente notificacionesCliente;

    private PasswordEncoder passwordEncoder;
    private RecuperacionContrasenaServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new RecuperacionContrasenaServiceImpl(
                repository, passwordEncoder, notificacionesCliente);
    }

    @Test
    void solicitaSinRevelarCorreoInexistente() {
        SolicitudRecuperacionDTO solicitud = new SolicitudRecuperacionDTO();
        solicitud.setCorreo("desconocido@example.com");
        when(repository.findByCorreoForUpdate("desconocido@example.com")).thenReturn(Optional.empty());

        var respuesta = service.solicitar(solicitud);

        assertThat(respuesta.mensaje()).contains("Si el correo existe");
    }

    @Test
    void generaCodigoHashYExpiracionDeDiezMinutos() {
        Credencial credencial = credencial();
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));
        when(repository.save(any(Credencial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SolicitudRecuperacionDTO solicitud = new SolicitudRecuperacionDTO();
        solicitud.setCorreo("cliente@pagoflex.cl");
        service.solicitar(solicitud);

        ArgumentCaptor<SolicitudCorreoNotificacionesDTO> captor =
                ArgumentCaptor.forClass(SolicitudCorreoNotificacionesDTO.class);
        verify(notificacionesCliente).enviarCodigoRecuperacion(captor.capture());
        assertThat(captor.getValue().codigo()).matches("\\d{6}");
        assertThat(credencial.getRecoveryCodeHash()).hasSize(64);
        assertThat(credencial.getRecoveryCodeExpiration())
                .isAfter(LocalDateTime.now().plusMinutes(9));
    }

    @Test
    void codigoCorrectoActualizaConBCryptEInvalidaCodigo() {
        Credencial credencial = credencial();
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));
        when(repository.save(any(Credencial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SolicitudRecuperacionDTO solicitud = new SolicitudRecuperacionDTO();
        solicitud.setCorreo("cliente@pagoflex.cl");
        service.solicitar(solicitud);
        ArgumentCaptor<SolicitudCorreoNotificacionesDTO> captor =
                ArgumentCaptor.forClass(SolicitudCorreoNotificacionesDTO.class);
        verify(notificacionesCliente).enviarCodigoRecuperacion(captor.capture());

        CambioContrasenaRecuperacionDTO cambio = new CambioContrasenaRecuperacionDTO();
        cambio.setCorreo("cliente@pagoflex.cl");
        cambio.setCodigo(captor.getValue().codigo());
        cambio.setNuevaContrasena("Nueva123");

        var respuesta = service.cambiar(cambio);

        assertThat(respuesta.mensaje()).contains("modificada");
        assertThat(credencial.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Nueva123", credencial.getPasswordHash())).isTrue();
        assertThat(credencial.isRecoveryCodeUsed()).isTrue();
        assertThat(credencial.getRecoveryCodeHash()).isNull();
    }

    @Test
    void rechazaCodigoIncorrecto() {
        Credencial credencial = credencial();
        credencial.setRecoveryCodeHash(sha256("123456"));
        credencial.setRecoveryCodeExpiration(LocalDateTime.now().plusMinutes(5));
        credencial.setRecoveryCodeUsed(false);
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));

        CambioContrasenaRecuperacionDTO cambio = new CambioContrasenaRecuperacionDTO();
        cambio.setCorreo("cliente@pagoflex.cl");
        cambio.setCodigo("654321");
        cambio.setNuevaContrasena("Nueva123");

        assertThatThrownBy(() -> service.cambiar(cambio))
                .isInstanceOf(CodigoRecuperacionInvalidoException.class);
    }

    @Test
    void rechazaCodigoExpirado() {
        Credencial credencial = credencial();
        credencial.setRecoveryCodeHash(sha256("123456"));
        credencial.setRecoveryCodeExpiration(LocalDateTime.now().minusMinutes(1));
        credencial.setRecoveryCodeUsed(false);
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));

        CambioContrasenaRecuperacionDTO cambio = new CambioContrasenaRecuperacionDTO();
        cambio.setCorreo("cliente@pagoflex.cl");
        cambio.setCodigo("123456");
        cambio.setNuevaContrasena("Nueva123");

        assertThatThrownBy(() -> service.cambiar(cambio))
                .isInstanceOf(CodigoRecuperacionInvalidoException.class);
    }

    @Test
    void rechazaCodigoReutilizado() {
        Credencial credencial = credencial();
        credencial.setRecoveryCodeUsed(true);
        credencial.setRecoveryCodeHash(null);
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));

        CambioContrasenaRecuperacionDTO cambio = new CambioContrasenaRecuperacionDTO();
        cambio.setCorreo("cliente@pagoflex.cl");
        cambio.setCodigo("123456");
        cambio.setNuevaContrasena("Nueva123");

        assertThatThrownBy(() -> service.cambiar(cambio))
                .isInstanceOf(CodigoRecuperacionInvalidoException.class);
    }

    @Test
    void errorDeCorreoInvalidaElCodigoGenerado() {
        Credencial credencial = credencial();
        when(repository.findByCorreoForUpdate("cliente@pagoflex.cl")).thenReturn(Optional.of(credencial));
        when(repository.save(any(Credencial.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new NotificacionesNoDisponibleException())
                .when(notificacionesCliente).enviarCodigoRecuperacion(any());

        SolicitudRecuperacionDTO solicitud = new SolicitudRecuperacionDTO();
        solicitud.setCorreo("cliente@pagoflex.cl");

        assertThatThrownBy(() -> service.solicitar(solicitud))
                .isInstanceOf(NotificacionesNoDisponibleException.class);
        assertThat(credencial.isRecoveryCodeUsed()).isTrue();
        assertThat(credencial.getRecoveryCodeHash()).isNull();
    }

    private Credencial credencial() {
        Credencial credencial = new Credencial();
        credencial.setUsuarioId(1L);
        credencial.setCorreo("cliente@pagoflex.cl");
        credencial.setPasswordHash(passwordEncoder.encode("Anterior123"));
        credencial.setActivo(true);
        credencial.setRecoveryCodeUsed(true);
        return credencial;
    }

    private String sha256(String value) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (byte current : bytes) {
                result.append(String.format("%02x", current));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
