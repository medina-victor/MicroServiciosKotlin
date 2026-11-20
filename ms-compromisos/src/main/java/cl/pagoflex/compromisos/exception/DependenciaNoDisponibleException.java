package cl.pagoflex.compromisos.exception;

public class DependenciaNoDisponibleException extends RuntimeException {
    public DependenciaNoDisponibleException(String dependencia, String detalle) {
        super("Dependencia " + dependencia + " no disponible: " + detalle);
    }
}
