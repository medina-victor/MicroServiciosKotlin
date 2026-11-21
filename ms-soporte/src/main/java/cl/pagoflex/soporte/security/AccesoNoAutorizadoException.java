package cl.pagoflex.soporte.security;

public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException() {
        super("No tiene permisos para acceder al recurso solicitado");
    }
}
