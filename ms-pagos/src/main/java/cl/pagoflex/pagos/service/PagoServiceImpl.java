package cl.pagoflex.pagos.service;

import cl.pagoflex.pagos.client.CompromisoClient;
import cl.pagoflex.pagos.client.EmpresaClient;
import cl.pagoflex.pagos.client.UsuarioClient;
import cl.pagoflex.pagos.dto.PagoRequestDTO;
import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.exception.PagoDuplicadoException;
import cl.pagoflex.pagos.exception.PagoNotFoundException;
import cl.pagoflex.pagos.exception.PagoOperacionException;
import cl.pagoflex.pagos.exception.CompromisoActualizacionException;
import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.Pago;
import cl.pagoflex.pagos.repository.PagoRepository;
import cl.pagoflex.pagos.security.AccesoNoAutorizadoException;
import cl.pagoflex.pagos.security.AccesoPropietario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final CompromisoClient compromisoClient;
    private final UsuarioClient usuarioClient;
    private final EmpresaClient empresaClient;

    public PagoServiceImpl(PagoRepository pagoRepository, CompromisoClient compromisoClient,
                           UsuarioClient usuarioClient, EmpresaClient empresaClient) {
        this.pagoRepository = pagoRepository;
        this.compromisoClient = compromisoClient;
        this.usuarioClient = usuarioClient;
        this.empresaClient = empresaClient;
    }

    @Override
    public PagoResponseDTO registrar(PagoRequestDTO request, AccesoPropietario acceso) {
        if ((acceso.esCliente() && !acceso.usuarioId().equals(request.getUsuarioId()))
                || (acceso.esEmpresa() && !acceso.empresaId().equals(request.getEmpresaId()))) {
            throw new AccesoNoAutorizadoException();
        }
        if (pagoRepository.existsByCompromisoIdAndEstado(request.getCompromisoId(), EstadoPago.EXITOSO)) {
            throw new PagoDuplicadoException("El compromiso ya tiene un pago EXITOSO");
        }
        CompromisoClient.CompromisoExterno compromiso = compromisoClient.buscarPorId(request.getCompromisoId());
        if (!request.getUsuarioId().equals(compromiso.usuarioId())) {
            throw new PagoOperacionException("El usuario del pago no corresponde al compromiso");
        }
        if (!request.getEmpresaId().equals(compromiso.empresaId())) {
            throw new PagoOperacionException("La empresa del pago no corresponde al compromiso");
        }
        if (compromiso.monto() == null || request.getMonto().compareTo(compromiso.monto()) != 0) {
            throw new PagoOperacionException("El monto del pago debe coincidir con el monto del compromiso");
        }
        if (!"PENDIENTE".equals(compromiso.estado()) && !"VENCIDO".equals(compromiso.estado())) {
            throw new PagoOperacionException("Solo se pueden registrar pagos para compromisos PENDIENTE o VENCIDO");
        }
        UsuarioClient.UsuarioExterno usuario = usuarioClient.buscarPorId(request.getUsuarioId());
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new PagoOperacionException("El usuario no está activo");
        }
        EmpresaClient.EmpresaExterna empresa = empresaClient.buscarPorId(request.getEmpresaId());
        if (!Boolean.TRUE.equals(empresa.activo())) {
            throw new PagoOperacionException("La empresa no está activa");
        }
        Pago pago = new Pago();
        pago.setCompromisoId(request.getCompromisoId());
        pago.setUsuarioId(request.getUsuarioId());
        pago.setEmpresaId(request.getEmpresaId());
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setDescripcion(request.getDescripcion());
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setReferenciaTransaccion(generarReferenciaUnica());
        return PagoResponseDTO.desde(pagoRepository.save(pago));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarTodos(AccesoPropietario acceso) {
        exigirPrivilegiado(acceso);
        return pagoRepository.findAll().stream().map(PagoResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO buscarPorId(Long id, AccesoPropietario acceso) {
        Pago pago = obtenerPago(id);
        acceso.exigirPropietario(pago.getUsuarioId(), pago.getEmpresaId());
        return PagoResponseDTO.desde(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorUsuario(Long usuarioId, AccesoPropietario acceso) {
        if (acceso.esCliente() && !acceso.usuarioId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException();
        }
        List<Pago> pagos = acceso.esEmpresa()
                ? pagoRepository.findByUsuarioIdAndEmpresaId(usuarioId, acceso.empresaId())
                : pagoRepository.findByUsuarioId(usuarioId);
        return pagos.stream().map(PagoResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorEmpresa(Long empresaId, AccesoPropietario acceso) {
        if (acceso.esEmpresa() && !acceso.empresaId().equals(empresaId)) {
            throw new AccesoNoAutorizadoException();
        }
        List<Pago> pagos = acceso.esCliente()
                ? pagoRepository.findByUsuarioIdAndEmpresaId(acceso.usuarioId(), empresaId)
                : pagoRepository.findByEmpresaId(empresaId);
        return pagos.stream().map(PagoResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorCompromiso(Long compromisoId, AccesoPropietario acceso) {
        List<Pago> pagos = acceso.esCliente()
                ? pagoRepository.findByCompromisoIdAndUsuarioId(compromisoId, acceso.usuarioId())
                : acceso.esEmpresa()
                    ? pagoRepository.findByCompromisoIdAndEmpresaId(compromisoId, acceso.empresaId())
                    : pagoRepository.findByCompromisoId(compromisoId);
        return pagos.stream().map(PagoResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> buscarPorEstado(EstadoPago estado, AccesoPropietario acceso) {
        List<Pago> pagos = acceso.esCliente()
                ? pagoRepository.findByEstadoAndUsuarioId(estado, acceso.usuarioId())
                : acceso.esEmpresa()
                    ? pagoRepository.findByEstadoAndEmpresaId(estado, acceso.empresaId())
                    : pagoRepository.findByEstado(estado);
        return pagos.stream().map(PagoResponseDTO::desde).toList();
    }

    @Override
    public PagoResponseDTO marcarExitoso(Long id) {
        Pago pago = pagoRepository.findByIdForUpdate(id).orElseThrow(() -> new PagoNotFoundException(id));
        if (pago.getEstado() == EstadoPago.EXITOSO) {
            return PagoResponseDTO.desde(pago);
        }
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PagoOperacionException("Solo se puede marcar como EXITOSO un pago PENDIENTE");
        }
        if (pagoRepository.existsByCompromisoIdAndEstado(pago.getCompromisoId(), EstadoPago.EXITOSO)) {
            throw new PagoDuplicadoException("El compromiso ya tiene un pago EXITOSO");
        }
        try {
            compromisoClient.marcarPagado(pago.getCompromisoId(), pago.getId());
        } catch (RuntimeException ex) {
            throw new CompromisoActualizacionException(pago.getCompromisoId(), ex);
        }
        pago.setEstado(EstadoPago.EXITOSO);
        return PagoResponseDTO.desde(pagoRepository.save(pago));
    }

    @Override
    public PagoResponseDTO rechazar(Long id) {
        Pago pago = obtenerPago(id);
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PagoOperacionException("Solo se puede rechazar un pago PENDIENTE");
        }
        pago.setEstado(EstadoPago.RECHAZADO);
        return PagoResponseDTO.desde(pagoRepository.save(pago));
    }

    @Override
    public PagoResponseDTO anular(Long id) {
        Pago pago = obtenerPago(id);
        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new PagoOperacionException("Solo se puede anular un pago PENDIENTE");
        }
        pago.setEstado(EstadoPago.ANULADO);
        return PagoResponseDTO.desde(pagoRepository.save(pago));
    }

    @Override
    public void eliminar(Long id) {
        Pago pago = obtenerPago(id);
        if (pago.getEstado() == EstadoPago.EXITOSO) {
            throw new PagoOperacionException("No se permite eliminar físicamente un pago EXITOSO");
        }
        pagoRepository.delete(pago);
    }

    private Pago obtenerPago(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new PagoNotFoundException(id));
    }

    private String generarReferenciaUnica() {
        String referencia;
        do {
            referencia = "PAG-" + UUID.randomUUID();
        } while (pagoRepository.existsByReferenciaTransaccion(referencia));
        return referencia;
    }

    private void exigirPrivilegiado(AccesoPropietario acceso) {
        if (!acceso.privilegiado()) {
            throw new AccesoNoAutorizadoException();
        }
    }
}
