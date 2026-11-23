package cl.pagoflex.notificaciones.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarRestricciones(
            ConstraintViolationException exception, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpo(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", request);
    }

    @ExceptionHandler(SmtpNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> manejarSmtp(
            SmtpNoDisponibleException exception, HttpServletRequest request) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), request);
    }

    @ExceptionHandler(NotificacionException.class)
    public ResponseEntity<ErrorResponse> manejarNotificacion(
            NotificacionException exception, HttpServletRequest request) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "No fue posible procesar la notificacion", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInterno(
            Exception exception, HttpServletRequest request) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno", request);
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String mensaje,
                                                     HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), status.value(),
                status.name(), mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
