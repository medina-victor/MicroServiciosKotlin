package cl.pagoflex.compromisos.exception;

import org.springframework.http.HttpStatus;

public class DependenciaRemotaException extends RuntimeException {
    private final HttpStatus status;

    public DependenciaRemotaException(String dependencia, int codigo) {
        super("Dependencia " + dependencia + " respondió con HTTP " + codigo);
        HttpStatus remoto = HttpStatus.resolve(codigo);
        this.status = remoto == null || remoto.is5xxServerError() ? HttpStatus.BAD_GATEWAY : remoto;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
