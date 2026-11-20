package cl.pagoflex.autenticacion.exception;

public class ServicioUsuariosNoDisponibleException extends RuntimeException {
    public ServicioUsuariosNoDisponibleException() {
        this("El servicio ms-usuarios no está disponible");
    }

    public ServicioUsuariosNoDisponibleException(String message) {
        super(message);
    }
}
