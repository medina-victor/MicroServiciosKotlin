package cl.pagoflex.pagos.exception;

public class CompromisoActualizacionException extends RuntimeException {
    public CompromisoActualizacionException(Long compromisoId, RuntimeException cause) {
        super("El pago quedó registrado como EXITOSO, pero no se pudo actualizar el compromiso "
                + compromisoId + ". Se requiere reconciliación.", cause);
    }
}
