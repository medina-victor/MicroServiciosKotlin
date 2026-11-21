package cl.pagoflex.soporte.service;

import cl.pagoflex.soporte.dto.TicketEstadoDTO;
import cl.pagoflex.soporte.dto.TicketRespuestaDTO;
import cl.pagoflex.soporte.dto.TicketSoporteRequestDTO;
import cl.pagoflex.soporte.dto.TicketSoporteResponseDTO;
import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.security.AccesoPropietario;

import java.util.List;

public interface TicketSoporteService {
    TicketSoporteResponseDTO crear(TicketSoporteRequestDTO request, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> listarTodas(AccesoPropietario acceso);
    TicketSoporteResponseDTO buscarPorId(Long id, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> buscarPorUsuario(Long usuarioId, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> buscarPorEmpresa(Long empresaId, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> buscarPorEstado(EstadoTicket estado, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> buscarPorCategoria(CategoriaSoporte categoria, AccesoPropietario acceso);
    List<TicketSoporteResponseDTO> buscarPorPrioridad(PrioridadTicket prioridad, AccesoPropietario acceso);
    TicketSoporteResponseDTO actualizar(Long id, TicketSoporteRequestDTO request, AccesoPropietario acceso);
    TicketSoporteResponseDTO cambiarEstado(Long id, TicketEstadoDTO dto);
    TicketSoporteResponseDTO responder(Long id, TicketRespuestaDTO dto);
    TicketSoporteResponseDTO cerrar(Long id);
    void eliminar(Long id);
}
