package cl.pagoflex.soporte.repository;

import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.model.TicketSoporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketSoporteRepository extends JpaRepository<TicketSoporte, Long> {
    List<TicketSoporte> findByUsuarioId(Long usuarioId);
    List<TicketSoporte> findByEmpresaId(Long empresaId);
    List<TicketSoporte> findByEstado(EstadoTicket estado);
    List<TicketSoporte> findByCategoria(CategoriaSoporte categoria);
    List<TicketSoporte> findByPrioridad(PrioridadTicket prioridad);
    List<TicketSoporte> findByUsuarioIdAndEstado(Long usuarioId, EstadoTicket estado);
}
