package cl.pagoflex.notificaciones.service;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.notificaciones.exception.SmtpNoDisponibleException;
import cl.pagoflex.notificaciones.service.impl.NotificacionServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NotificacionServiceTest {
    private JavaMailSender mailSender;
    private NotificacionServiceImpl service;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        service = new NotificacionServiceImpl(mailSender, "smtp.gmail.com",
                "pagoflex@example.com", "app-password", "Pagoflex <pagoflex@example.com>");
        mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void enviaCodigoRecuperacion() {
        RespuestaNotificacionDTO respuesta = service.enviarCodigoRecuperacion(
                new SolicitudRecuperacionDTO("usuario@example.com", "123456"));

        assertTrue(respuesta.enviado());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void enviaConfirmacionCambioContrasena() {
        RespuestaNotificacionDTO respuesta = service.enviarConfirmacionCambioContrasena(
                new SolicitudCambioContrasenaDTO("usuario@example.com"));

        assertTrue(respuesta.enviado());
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void rechazaConfiguracionSmtpIncompleta() {
        NotificacionServiceImpl incompleto = new NotificacionServiceImpl(
                mailSender, "smtp.gmail.com", "", "", "");

        assertThrows(SmtpNoDisponibleException.class,
                () -> incompleto.enviarCodigoRecuperacion(
                        new SolicitudRecuperacionDTO("usuario@example.com", "123456")));
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void transformaErrorSmtpEnServicioNoDisponible() {
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThrows(SmtpNoDisponibleException.class,
                () -> service.enviarCodigoRecuperacion(
                        new SolicitudRecuperacionDTO("usuario@example.com", "123456")));
    }
}
