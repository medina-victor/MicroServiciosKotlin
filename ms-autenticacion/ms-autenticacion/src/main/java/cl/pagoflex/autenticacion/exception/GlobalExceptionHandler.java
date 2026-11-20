package cl.pagoflex.autenticacion.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({CredencialNotFoundException.class, UsuarioNoEncontradoException.class})
    public ResponseEntity<ErrorResponse> noEncontrado(RuntimeException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CredencialDuplicadaException.class)
    public ResponseEntity<ErrorResponse> conflicto(CredencialDuplicadaException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({CredencialesInvalidasException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> credencialesInvalidas(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.UNAUTHORIZED, "Correo o contraseña inválidos", request);
    }

    @ExceptionHandler({UsuarioInactivoException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> autenticacionInvalida(Exception ex, HttpServletRequest request) {
        String mensaje = ex instanceof UsuarioInactivoException ? ex.getMessage() : "Autenticación requerida";
        return respuesta(HttpStatus.UNAUTHORIZED, mensaje, request);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> tokenInvalido(TokenInvalidoException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accesoDenegado(AccessDeniedException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", request);
    }

    @ExceptionHandler(ServicioUsuariosNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> servicioNoDisponible(ServicioUsuariosNoDisponibleException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(RespuestaServicioUsuariosException.class)
    public ResponseEntity<ErrorResponse> respuestaUsuarios(RespuestaServicioUsuariosException ex, HttpServletRequest request) {
        return respuesta(ex.getStatus(), ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage()).distinct().collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> solicitudInvalida(Exception ex, HttpServletRequest request) {
        HttpStatus status = ex instanceof DataIntegrityViolationException ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        String mensaje = ex instanceof DataIntegrityViolationException
                ? "Existe un conflicto con los datos de credenciales" : ex.getMessage();
        return respuesta(status, mensaje, request);
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
