package cl.pagoflex.autenticacion.exception;

public class ServicioUsuariosNoDisponibleException extends RuntimeException {
    public ServicioUsuariosNoDisponibleException() { super("El servicio ms-usuarios no está disponible"); }
}
