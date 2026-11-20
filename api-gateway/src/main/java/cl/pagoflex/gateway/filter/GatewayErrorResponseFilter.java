package cl.pagoflex.gateway.filter;

import cl.pagoflex.gateway.exception.GatewayErrorResponseWriter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.reactivestreams.Publisher;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class GatewayErrorResponseFilter implements GlobalFilter {
    private final GatewayErrorResponseWriter responseWriter;

    public GatewayErrorResponseFilter(GatewayErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                HttpStatusCode status = getStatusCode();
                if (status == null || !status.isError()) {
                    return super.writeWith(body);
                }

                HttpStatusCode normalizedStatus = status.value() == HttpStatus.SERVICE_UNAVAILABLE.value()
                        ? HttpStatus.BAD_GATEWAY : status;
                return Flux.from(body)
                        .doOnNext(DataBufferUtils::release)
                        .then(responseWriter.write(
                                getDelegate(),
                                exchange.getRequest().getPath().value(),
                                normalizedStatus));
            }
        };
        return chain.filter(exchange.mutate().response(response).build());
    }
}
