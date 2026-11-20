package cl.pagoflex.autenticacion.exception;

public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() { super("Correo o contraseña inválidos"); }
}
