package cl.pagoflex.pagos.repository;

import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.MetodoPago;
import cl.pagoflex.pagos.model.Pago;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class PagoRepositoryTest {

    @Autowired
    private PagoRepository repository;

    @Test
    void buscaPorIdentificadoresEstadoYReferencia() {
        Pago pago = pago(100L, "PAG-1", EstadoPago.PENDIENTE);
        Pago guardado = repository.saveAndFlush(pago);

        assertThat(repository.findByUsuarioId(10L)).containsExactly(guardado);
        assertThat(repository.findByEmpresaId(20L)).containsExactly(guardado);
        assertThat(repository.findByCompromisoId(100L)).containsExactly(guardado);
        assertThat(repository.findByEstado(EstadoPago.PENDIENTE)).containsExactly(guardado);
        assertThat(repository.findByReferenciaTransaccion("PAG-1")).isPresent();
        assertThat(repository.existsByCompromisoId(100L)).isTrue();
        assertThat(repository.existsByCompromisoIdAndEstado(100L, EstadoPago.EXITOSO)).isFalse();
    }

    @Test
    void impideReferenciasDuplicadas() {
        repository.saveAndFlush(pago(100L, "PAG-unica", EstadoPago.PENDIENTE));

        assertThatThrownBy(() -> repository.saveAndFlush(pago(101L, "PAG-unica", EstadoPago.PENDIENTE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void permiteMasDeUnPagoFallidoPeroConsignaEstadoExitoso() {
        repository.saveAllAndFlush(List.of(
                pago(100L, "PAG-pendiente", EstadoPago.PENDIENTE),
                pago(100L, "PAG-exitoso", EstadoPago.EXITOSO)));

        assertThat(repository.existsByCompromisoIdAndEstado(100L, EstadoPago.EXITOSO)).isTrue();
        assertThat(repository.findByCompromisoId(100L)).hasSize(2);
    }

    private Pago pago(Long compromisoId, String referencia, EstadoPago estado) {
        Pago pago = new Pago();
        pago.setCompromisoId(compromisoId);
        pago.setUsuarioId(10L);
        pago.setEmpresaId(20L);
        pago.setMonto(new BigDecimal("100.00"));
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setEstado(estado);
        pago.setReferenciaTransaccion(referencia);
        return pago;
    }
}
