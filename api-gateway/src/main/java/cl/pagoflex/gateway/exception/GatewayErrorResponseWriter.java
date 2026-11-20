package cl.pagoflex.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class GatewayErrorResponseWriter {
    private final ObjectMapper objectMapper;

    public GatewayErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatus status) {
        return write(exchange.getResponse(), exchange.getRequest().getPath().value(), status);
    }

    public Mono<Void> write(ServerHttpResponse response, String path, HttpStatusCode status) {
        HttpStatus resolvedStatus = HttpStatus.resolve(status.value());
        String message = messageFor(resolvedStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolvedStatus);
        return write(response, path, status, message);
    }

    public Mono<Void> write(ServerHttpResponse response, String path, HttpStatusCode status, String message) {
        if (response.isCommitted()) {
            return Mono.error(new IllegalStateException("No se puede escribir un error después de confirmar la respuesta"));
        }

        HttpStatus resolvedStatus = HttpStatus.resolve(status.value());
        String errorName = resolvedStatus == null ? "Error" : resolvedStatus.getReasonPhrase();
        GatewayErrorResponse body = new GatewayErrorResponse(
                Instant.now(), status.value(), errorName, message, path);
        byte[] content;
        try {
            content = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException ex) {
            return Mono.error(ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setCacheControl("no-store");
        response.getHeaders().remove(HttpHeaders.CONTENT_LENGTH);
        response.getHeaders().remove(HttpHeaders.CONTENT_ENCODING);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(content)));
    }

    public String messageFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Solicitud incorrecta";
            case UNAUTHORIZED -> "Autenticación requerida o token inválido";
            case FORBIDDEN -> "Acceso prohibido";
            case NOT_FOUND -> "Ruta o recurso no encontrado";
            case BAD_GATEWAY -> "Servicio destino no disponible";
            case GATEWAY_TIMEOUT -> "Tiempo de espera agotado";
            default -> status.is5xxServerError() ? "Error interno" : status.getReasonPhrase();
        };
    }
}
