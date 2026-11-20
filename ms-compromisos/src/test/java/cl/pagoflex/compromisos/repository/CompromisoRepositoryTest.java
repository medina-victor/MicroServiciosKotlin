package cl.pagoflex.compromisos.repository;

import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect")
class CompromisoRepositoryTest {

    @Autowired
    private CompromisoRepository repository;

    @Test
    void guardaYBuscaPorTodosLosCriterios() {
        Compromiso compromiso = nuevo();
        Compromiso guardado = repository.save(compromiso);

        assertThat(repository.findById(guardado.getId())).isPresent();
        assertThat(repository.findByUsuarioId(1L)).hasSize(1);
        assertThat(repository.findByEmpresaId(2L)).hasSize(1);
        assertThat(repository.findByEstado(EstadoCompromiso.PENDIENTE)).hasSize(1);
        assertThat(repository.buscarVencidos(LocalDate.now())).isEmpty();
    }

    @Test
    void encuentraCompromisosVencidosPorFecha() {
        Compromiso compromiso = nuevo();
        compromiso.setFechaVencimiento(LocalDate.now().minusDays(1));
        repository.save(compromiso);

        assertThat(repository.buscarVencidos(LocalDate.now())).hasSize(1);
    }

    private Compromiso nuevo() {
        Compromiso compromiso = new Compromiso();
        compromiso.setUsuarioId(1L);
        compromiso.setEmpresaId(2L);
        compromiso.setDescripcion("Cuota");
        compromiso.setMonto(new BigDecimal("1000"));
        compromiso.setFechaVencimiento(LocalDate.now().plusDays(10));
        compromiso.setEstado(EstadoCompromiso.PENDIENTE);
        return compromiso;
    }
}
