package cl.pagoflex.soporte.exception;

public class TicketNoEncontradoException extends RuntimeException {
    public TicketNoEncontradoException(Long id) {
        super("Ticket de soporte no encontrado con identificador " + id);
    }
}
