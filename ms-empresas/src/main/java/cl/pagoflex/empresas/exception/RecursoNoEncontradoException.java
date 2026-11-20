package cl.pagoflex.empresas.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(Long id) {
        super("No se encontró la empresa con ID " + id);
    }

    public RecursoNoEncontradoException(String rut) {
        super("No se encontró la empresa con RUT " + rut);
    }
}
