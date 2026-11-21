package cl.pagoflex.pagos.service;

import cl.pagoflex.pagos.dto.PagoRequestDTO;
import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.security.AccesoPropietario;

import java.util.List;

public interface PagoService {
    PagoResponseDTO registrar(PagoRequestDTO request, AccesoPropietario acceso);
    List<PagoResponseDTO> listarTodos(AccesoPropietario acceso);
    PagoResponseDTO buscarPorId(Long id, AccesoPropietario acceso);
    List<PagoResponseDTO> buscarPorUsuario(Long usuarioId, AccesoPropietario acceso);
    List<PagoResponseDTO> buscarPorEmpresa(Long empresaId, AccesoPropietario acceso);
    List<PagoResponseDTO> buscarPorCompromiso(Long compromisoId, AccesoPropietario acceso);
    List<PagoResponseDTO> buscarPorEstado(EstadoPago estado, AccesoPropietario acceso);
    PagoResponseDTO marcarExitoso(Long id);
    PagoResponseDTO rechazar(Long id);
    PagoResponseDTO anular(Long id);
    void eliminar(Long id);
}
