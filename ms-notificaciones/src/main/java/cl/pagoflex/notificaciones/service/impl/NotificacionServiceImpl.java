package cl.pagoflex.notificaciones.service.impl;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.notificaciones.exception.NotificacionException;
import cl.pagoflex.notificaciones.exception.SmtpNoDisponibleException;
import cl.pagoflex.notificaciones.service.NotificacionService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificacionServiceImpl implements NotificacionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String mailHost;
    private final String mailUsername;
    private final String mailPassword;
    private final String mailFrom;

    public NotificacionServiceImpl(JavaMailSender mailSender,
                                   @Value("${spring.mail.host}") String mailHost,
                                   @Value("${spring.mail.username:}") String mailUsername,
                                   @Value("${spring.mail.password:}") String mailPassword,
                                   @Value("${mail.from:}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.mailFrom = mailFrom;
    }

    @Override
    public RespuestaNotificacionDTO enviarCodigoRecuperacion(SolicitudRecuperacionDTO solicitud) {
        validarConfiguracionSmtp();
        String asunto = "PagoFlex - Codigo de recuperacion";
        String contenido = plantillaRecuperacion(solicitud.codigo());
        enviarHtml(solicitud.correo(), asunto, contenido);
        return new RespuestaNotificacionDTO(true, "Correo de recuperacion enviado correctamente");
    }

    @Override
    public RespuestaNotificacionDTO enviarConfirmacionCambioContrasena(
            SolicitudCambioContrasenaDTO solicitud) {
        validarConfiguracionSmtp();
        String asunto = "PagoFlex - Cambio de contrasena confirmado";
        String contenido = plantillaCambioContrasena();
        enviarHtml(solicitud.correo(), asunto, contenido);
        return new RespuestaNotificacionDTO(true, "Correo de confirmacion enviado correctamente");
    }

    private void validarConfiguracionSmtp() {
        if (esVacio(mailHost) || esVacio(mailUsername) || esVacio(mailPassword)
                || esVacio(mailFrom)) {
            LOGGER.error("Configuracion SMTP incompleta");
            throw new SmtpNoDisponibleException(
                    "La configuracion SMTP esta incompleta");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void enviarHtml(String destinatario, String asunto, String contenido) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);
            mailSender.send(mensaje);
        } catch (MessagingException exception) {
            LOGGER.error("No fue posible construir el correo de PagoFlex");
            throw new NotificacionException("No fue posible construir la notificacion", exception);
        } catch (MailException exception) {
            LOGGER.error("No fue posible enviar el correo mediante SMTP");
            throw new SmtpNoDisponibleException(
                    "El servicio SMTP no esta disponible", exception);
        }
    }

    private String plantillaRecuperacion(String codigo) {
        return """
                <!doctype html>
                <html><body style="margin:0;background:#f4f7fb;font-family:Arial,sans-serif">
                <div style="max-width:600px;margin:32px auto;background:#fff;border-radius:12px;overflow:hidden">
                  <div style="background:#153e75;color:#fff;padding:24px;text-align:center"><h1>PagoFlex</h1></div>
                  <div style="padding:32px;color:#263238">
                    <h2>Recuperacion de contrasena</h2>
                    <p>Solicitaste recuperar el acceso a tu cuenta. Utiliza el siguiente codigo temporal:</p>
                    <div style="font-size:32px;font-weight:bold;text-align:center;letter-spacing:8px;
                                background:#eef4ff;color:#153e75;padding:20px;border-radius:8px">%s</div>
                    <p><strong>Este codigo es temporal.</strong> No lo compartas con nadie.</p>
                    <p>Si tu no solicitaste esta recuperacion, ignora este correo.</p>
                  </div>
                </div></body></html>
                """.formatted(codigo);
    }

    private String plantillaCambioContrasena() {
        return """
                <!doctype html>
                <html><body style="margin:0;background:#f4f7fb;font-family:Arial,sans-serif">
                <div style="max-width:600px;margin:32px auto;background:#fff;border-radius:12px;overflow:hidden">
                  <div style="background:#153e75;color:#fff;padding:24px;text-align:center"><h1>PagoFlex</h1></div>
                  <div style="padding:32px;color:#263238">
                    <h2>Contrasena modificada correctamente</h2>
                    <p>La contrasena de tu cuenta de PagoFlex fue modificada correctamente.</p>
                    <p>Si tu no realizaste este cambio, contacta inmediatamente al equipo de soporte.</p>
                  </div>
                </div></body></html>
                """;
    }
}
