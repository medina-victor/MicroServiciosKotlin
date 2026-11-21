package cl.pagoflex.pagos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CompromisoClient extends ServicioRestClient {
    public CompromisoClient(RestClient.Builder builder,
                            @Value("${services.compromisos.url:http://ms-compromisos}") String baseUrl) {
        super(builder, "ms-compromisos", baseUrl);
    }

    public CompromisoExterno buscarPorId(Long id) {
        CompromisoExterno compromiso = ejecutar(() -> restClient.get().uri("/api/compromisos/{id}", id)
                .retrieve().body(CompromisoExterno.class));
        validarId(id, compromiso.id());
        return compromiso;
    }

    public void marcarPagado(Long id, Long pagoId) {
        ejecutar(() -> restClient.patch().uri("/api/compromisos/{id}/pagar?pagoId={pagoId}", id, pagoId)
                .retrieve().toBodilessEntity());
    }

    public record CompromisoExterno(Long id, Long usuarioId, Long empresaId, BigDecimal monto, String estado) {
    }
}
