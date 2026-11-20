package cl.pagoflex.pagos.service;

import cl.pagoflex.pagos.dto.PagoRequestDTO;
import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.model.EstadoPago;

import java.util.List;

public interface PagoService {
    PagoResponseDTO registrar(PagoRequestDTO request);
    List<PagoResponseDTO> listarTodos();
    PagoResponseDTO buscarPorId(Long id);
    List<PagoResponseDTO> buscarPorUsuario(Long usuarioId);
    List<PagoResponseDTO> buscarPorEmpresa(Long empresaId);
    List<PagoResponseDTO> buscarPorCompromiso(Long compromisoId);
    List<PagoResponseDTO> buscarPorEstado(EstadoPago estado);
    PagoResponseDTO marcarExitoso(Long id);
    PagoResponseDTO rechazar(Long id);
    PagoResponseDTO anular(Long id);
    void eliminar(Long id);
}
