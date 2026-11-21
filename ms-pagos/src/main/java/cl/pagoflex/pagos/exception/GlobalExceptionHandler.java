package cl.pagoflex.pagos.exception;

import cl.pagoflex.pagos.security.AccesoNoAutorizadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PagoNotFoundException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(PagoNotFoundException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(
            AccesoNoAutorizadoException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(DependenciaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> dependenciaNoDisponible(
            DependenciaNoDisponibleException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(DependenciaRemotaException.class)
    public ResponseEntity<ErrorResponse> dependenciaRemota(
            DependenciaRemotaException ex, HttpServletRequest request) {
        return respuesta(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(CompromisoActualizacionException.class)
    public ResponseEntity<ErrorResponse> compromisoNoActualizado(
            CompromisoActualizacionException ex, HttpServletRequest request) {
        LOGGER.error("Pago pendiente requiere confirmación del compromiso", ex);
        return respuesta(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler({PagoDuplicadoException.class, PagoOperacionException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> conflicto(Exception ex, HttpServletRequest request) {
        String mensaje = ex instanceof DataIntegrityViolationException
                ? "Existe un conflicto con los datos del pago"
                : ex.getMessage();
        return respuesta(HttpStatus.CONFLICT, mensaje, request);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> solicitudInvalida(Exception ex, HttpServletRequest request) {
        String mensaje = ex instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()).distinct().collect(Collectors.joining("; "))
                : ex instanceof MethodArgumentTypeMismatchException
                ? "La solicitud contiene parámetros inválidos"
                : ex instanceof HttpMessageNotReadableException
                ? "La solicitud contiene datos inválidos"
                : ex.getMessage();
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> interno(Exception ex, HttpServletRequest request) {
        LOGGER.error("Error interno al procesar {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", request);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI()));
    }
}
