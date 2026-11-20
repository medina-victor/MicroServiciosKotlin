package cl.pagoflex.autenticacion.exception;

public class UsuarioInactivoException extends RuntimeException {
    public UsuarioInactivoException() { super("El usuario está inactivo"); }
}
