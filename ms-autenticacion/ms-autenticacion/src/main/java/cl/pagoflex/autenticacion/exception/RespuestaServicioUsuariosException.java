package cl.pagoflex.autenticacion.exception;

import org.springframework.http.HttpStatus;

public class RespuestaServicioUsuariosException extends RuntimeException {
    private final HttpStatus status;

    public RespuestaServicioUsuariosException(int codigo) {
        super("ms-usuarios respondió con HTTP " + codigo);
        HttpStatus remoto = HttpStatus.resolve(codigo);
        this.status = remoto == null || remoto.is5xxServerError() ? HttpStatus.BAD_GATEWAY : remoto;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
