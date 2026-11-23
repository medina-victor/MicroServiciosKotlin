package cl.pagoflex.autenticacion.exception;

public class NotificacionesNoDisponibleException extends RuntimeException {
    public NotificacionesNoDisponibleException() {
        super("El servicio de correo no esta disponible");
    }
}
