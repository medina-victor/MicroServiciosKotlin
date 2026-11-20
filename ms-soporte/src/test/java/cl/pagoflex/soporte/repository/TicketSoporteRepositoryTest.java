package cl.pagoflex.soporte.repository;

import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.model.TicketSoporte;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TicketSoporteRepositoryTest {

    @Autowired
    private TicketSoporteRepository repository;

    @Test
    void guardaYBuscaPorFiltros() {
        TicketSoporte ticket = nuevo();
        TicketSoporte guardado = repository.save(ticket);

        assertThat(guardado.getId()).isNotNull();
        assertThat(repository.findByUsuarioId(1L)).hasSize(1);
        assertThat(repository.findByEmpresaId(2L)).hasSize(1);
        assertThat(repository.findByEstado(EstadoTicket.ABIERTO)).hasSize(1);
        assertThat(repository.findByCategoria(CategoriaSoporte.PAGOS)).hasSize(1);
        assertThat(repository.findByPrioridad(PrioridadTicket.MEDIA)).hasSize(1);
        assertThat(repository.findByUsuarioIdAndEstado(1L, EstadoTicket.ABIERTO)).hasSize(1);
    }

    private TicketSoporte nuevo() {
        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuarioId(1L);
        ticket.setEmpresaId(2L);
        ticket.setAsunto("Pago pendiente");
        ticket.setDescripcion("La consulta sigue pendiente");
        ticket.setCategoria(CategoriaSoporte.PAGOS);
        ticket.setPrioridad(PrioridadTicket.MEDIA);
        ticket.setEstado(EstadoTicket.ABIERTO);
        return ticket;
    }
}
