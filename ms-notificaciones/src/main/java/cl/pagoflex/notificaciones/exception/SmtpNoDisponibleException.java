package cl.pagoflex.notificaciones.exception;

public class SmtpNoDisponibleException extends RuntimeException {
    public SmtpNoDisponibleException(String message) {
        super(message);
    }

    public SmtpNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
