package cl.pagoflex.pagos.exception;

public class PagoDuplicadoException extends RuntimeException {
    public PagoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
