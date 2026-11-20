package cl.pagoflex.pagos.exception;

public class PagoNotFoundException extends RuntimeException {
    public PagoNotFoundException(Long id) {
        super("No se encontró el pago con identificador " + id);
    }
}
