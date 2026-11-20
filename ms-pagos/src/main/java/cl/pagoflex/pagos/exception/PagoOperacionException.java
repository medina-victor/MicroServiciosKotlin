package cl.pagoflex.pagos.exception;

public class PagoOperacionException extends RuntimeException {
    public PagoOperacionException(String mensaje) {
        super(mensaje);
    }
}
