package cl.pagoflex.gateway.exception;

import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.ReadTimeoutException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GatewayExceptionHandler implements WebExceptionHandler {
    private final GatewayErrorResponseWriter responseWriter;

    public GatewayExceptionHandler(GatewayErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable exception) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(exception);
        }
        return responseWriter.write(exchange, statusFor(exception));
    }

    private HttpStatus statusFor(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof TimeoutException
                    || cause instanceof SocketTimeoutException
                    || cause instanceof ConnectTimeoutException
                    || cause instanceof ReadTimeoutException) {
                return HttpStatus.GATEWAY_TIMEOUT;
            }
            if (cause instanceof NotFoundException
                    || cause instanceof ConnectException
                    || cause instanceof WebClientRequestException
                    || cause instanceof IOException) {
                return HttpStatus.BAD_GATEWAY;
            }
        }

        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ResponseStatusException statusException) {
                int statusCode = statusException.getStatusCode().value();
                if (statusCode == HttpStatus.SERVICE_UNAVAILABLE.value()) {
                    return HttpStatus.BAD_GATEWAY;
                }
                HttpStatus status = HttpStatus.resolve(statusCode);
                return status != null && (status.is4xxClientError() || status.is5xxServerError())
                        ? status : HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
