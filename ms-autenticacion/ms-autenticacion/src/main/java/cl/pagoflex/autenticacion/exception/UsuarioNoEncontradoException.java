package cl.pagoflex.autenticacion.exception;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException() { super("El usuario no existe en ms-usuarios"); }
}
