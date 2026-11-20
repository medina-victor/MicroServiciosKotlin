package cl.pagoflex.pagos.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private final LocalDateTime fecha;
    private final int status;
    private final String error;
    private final String mensaje;
    private final String path;

    public ErrorResponse(LocalDateTime fecha, int status, String error, String mensaje, String path) {
        this.fecha = fecha;
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.path = path;
    }

    public LocalDateTime getFecha() { return fecha; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMensaje() { return mensaje; }
    public String getPath() { return path; }
}
