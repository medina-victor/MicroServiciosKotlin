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
    List<TicketSoporte> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    List<TicketSoporte> findByEstado(EstadoTicket estado);
    List<TicketSoporte> findByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);
    List<TicketSoporte> findByCategoria(CategoriaSoporte categoria);
    List<TicketSoporte> findByUsuarioIdAndCategoria(Long usuarioId, CategoriaSoporte categoria);
    List<TicketSoporte> findByEmpresaIdAndCategoria(Long empresaId, CategoriaSoporte categoria);
    List<TicketSoporte> findByPrioridad(PrioridadTicket prioridad);
    List<TicketSoporte> findByUsuarioIdAndPrioridad(Long usuarioId, PrioridadTicket prioridad);
    List<TicketSoporte> findByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);
    List<TicketSoporte> findByUsuarioIdAndEstado(Long usuarioId, EstadoTicket estado);
}
