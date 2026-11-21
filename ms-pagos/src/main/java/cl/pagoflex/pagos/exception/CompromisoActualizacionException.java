package cl.pagoflex.pagos.exception;

public class CompromisoActualizacionException extends RuntimeException {
    public CompromisoActualizacionException(Long compromisoId, RuntimeException cause) {
        super("No se pudo confirmar el compromiso " + compromisoId
                + "; el pago permanece PENDIENTE y se puede reintentar con el mismo identificador.", cause);
    }
}
