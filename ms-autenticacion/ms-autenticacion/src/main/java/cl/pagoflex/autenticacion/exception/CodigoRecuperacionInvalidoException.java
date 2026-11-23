package cl.pagoflex.autenticacion.exception;

public class CodigoRecuperacionInvalidoException extends RuntimeException {
    public CodigoRecuperacionInvalidoException() {
        super("El codigo de recuperacion no es valido");
    }
}
