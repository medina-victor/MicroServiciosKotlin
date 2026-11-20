package cl.pagoflex.soporte.service;

import cl.pagoflex.soporte.dto.TicketEstadoDTO;
import cl.pagoflex.soporte.dto.TicketRespuestaDTO;
import cl.pagoflex.soporte.dto.TicketSoporteRequestDTO;
import cl.pagoflex.soporte.dto.TicketSoporteResponseDTO;
import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;

import java.util.List;

public interface TicketSoporteService {
    TicketSoporteResponseDTO crear(TicketSoporteRequestDTO request);
    List<TicketSoporteResponseDTO> listarTodas();
    TicketSoporteResponseDTO buscarPorId(Long id);
    List<TicketSoporteResponseDTO> buscarPorUsuario(Long usuarioId);
    List<TicketSoporteResponseDTO> buscarPorEmpresa(Long empresaId);
    List<TicketSoporteResponseDTO> buscarPorEstado(EstadoTicket estado);
    List<TicketSoporteResponseDTO> buscarPorCategoria(CategoriaSoporte categoria);
    List<TicketSoporteResponseDTO> buscarPorPrioridad(PrioridadTicket prioridad);
    TicketSoporteResponseDTO actualizar(Long id, TicketSoporteRequestDTO request);
    TicketSoporteResponseDTO cambiarEstado(Long id, TicketEstadoDTO dto);
    TicketSoporteResponseDTO responder(Long id, TicketRespuestaDTO dto);
    TicketSoporteResponseDTO cerrar(Long id);
    void eliminar(Long id);
}
