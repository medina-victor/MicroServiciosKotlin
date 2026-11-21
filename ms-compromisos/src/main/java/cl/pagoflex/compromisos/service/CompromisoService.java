package cl.pagoflex.compromisos.service;

import cl.pagoflex.compromisos.client.EmpresaClient;
import cl.pagoflex.compromisos.client.UsuarioClient;
import cl.pagoflex.compromisos.dto.CompromisoRequestDTO;
import cl.pagoflex.compromisos.dto.CompromisoResponseDTO;
import cl.pagoflex.compromisos.exception.RecursoDuplicadoException;
import cl.pagoflex.compromisos.exception.RecursoNoEncontradoException;
import cl.pagoflex.compromisos.exception.SolicitudInvalidaException;
import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import cl.pagoflex.compromisos.repository.CompromisoRepository;
import cl.pagoflex.compromisos.security.AccesoNoAutorizadoException;
import cl.pagoflex.compromisos.security.AccesoPropietario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CompromisoService {

    private final CompromisoRepository compromisoRepository;
    private final UsuarioClient usuarioClient;
    private final EmpresaClient empresaClient;

    public CompromisoService(CompromisoRepository compromisoRepository,
                             UsuarioClient usuarioClient, EmpresaClient empresaClient) {
        this.compromisoRepository = compromisoRepository;
        this.usuarioClient = usuarioClient;
        this.empresaClient = empresaClient;
    }

    public CompromisoResponseDTO registrar(CompromisoRequestDTO request, AccesoPropietario acceso) {
        validarRequest(request);
        if ((acceso.esCliente() && !acceso.usuarioId().equals(request.getUsuarioId()))
                || (acceso.esEmpresa() && !acceso.empresaId().equals(request.getEmpresaId()))) {
            throw new AccesoNoAutorizadoException();
        }
        UsuarioClient.UsuarioExterno usuario = usuarioClient.buscarPorId(request.getUsuarioId());
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new SolicitudInvalidaException("El usuario no está activo");
        }
        EmpresaClient.EmpresaExterna empresa = empresaClient.buscarPorId(request.getEmpresaId());
        if (!Boolean.TRUE.equals(empresa.activo())) {
            throw new SolicitudInvalidaException("La empresa no está activa");
        }
        Compromiso compromiso = new Compromiso();
        aplicarDatos(compromiso, request);
        compromiso.setEstado(EstadoCompromiso.PENDIENTE);
        return CompromisoResponseDTO.desde(compromisoRepository.save(compromiso));
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> listarTodas(AccesoPropietario acceso) {
        exigirPrivilegiado(acceso);
        actualizarVencidos();
        return compromisoRepository.findAll().stream().map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public CompromisoResponseDTO buscarPorId(Long id, AccesoPropietario acceso) {
        actualizarVencidos();
        Compromiso compromiso = obtenerCompromiso(id);
        acceso.exigirPropietario(compromiso.getUsuarioId(), compromiso.getEmpresaId());
        return CompromisoResponseDTO.desde(compromiso);
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorUsuario(Long usuarioId, AccesoPropietario acceso) {
        if (acceso.esCliente() && !acceso.usuarioId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException();
        }
        actualizarVencidos();
        List<Compromiso> compromisos = acceso.esEmpresa()
                ? compromisoRepository.findByUsuarioIdAndEmpresaId(usuarioId, acceso.empresaId())
                : compromisoRepository.findByUsuarioId(usuarioId);
        return compromisos.stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorEmpresa(Long empresaId, AccesoPropietario acceso) {
        if (acceso.esEmpresa() && !acceso.empresaId().equals(empresaId)) {
            throw new AccesoNoAutorizadoException();
        }
        actualizarVencidos();
        List<Compromiso> compromisos = acceso.esCliente()
                ? compromisoRepository.findByUsuarioIdAndEmpresaId(acceso.usuarioId(), empresaId)
                : compromisoRepository.findByEmpresaId(empresaId);
        return compromisos.stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorEstado(EstadoCompromiso estado, AccesoPropietario acceso) {
        actualizarVencidos();
        List<Compromiso> compromisos = acceso.esCliente()
                ? compromisoRepository.findByEstadoAndUsuarioId(estado, acceso.usuarioId())
                : acceso.esEmpresa()
                    ? compromisoRepository.findByEstadoAndEmpresaId(estado, acceso.empresaId())
                    : compromisoRepository.findByEstado(estado);
        return compromisos.stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    public CompromisoResponseDTO actualizar(Long id, CompromisoRequestDTO request, AccesoPropietario acceso) {
        Compromiso compromiso = obtenerCompromiso(id);
        actualizar(request, acceso, compromiso);
        return CompromisoResponseDTO.desde(compromisoRepository.save(compromiso));
    }

    private void actualizar(CompromisoRequestDTO request, AccesoPropietario acceso,
                            Compromiso compromiso) {
        acceso.exigirPropietario(compromiso.getUsuarioId(), compromiso.getEmpresaId());
        validarRequest(request);
        if (!acceso.privilegiado()
                && (!compromiso.getUsuarioId().equals(request.getUsuarioId())
                || !compromiso.getEmpresaId().equals(request.getEmpresaId()))) {
            throw new AccesoNoAutorizadoException();
        }
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO
                || compromiso.getEstado() == EstadoCompromiso.CANCELADO) {
            throw new RecursoDuplicadoException("No se puede actualizar un compromiso PAGADO o CANCELADO");
        }
        if (!compromiso.getUsuarioId().equals(request.getUsuarioId())) {
            validarUsuarioActivo(request.getUsuarioId());
        }
        if (!compromiso.getEmpresaId().equals(request.getEmpresaId())) {
            validarEmpresaActiva(request.getEmpresaId());
        }
        aplicarDatos(compromiso, request);
    }

    public CompromisoResponseDTO pagar(Long id) {
        return pagar(id, null);
    }

    public CompromisoResponseDTO pagar(Long id, Long pagoId) {
        Compromiso compromiso = compromisoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));
        if (compromiso.getEstado() == EstadoCompromiso.CANCELADO) {
            throw new RecursoDuplicadoException("No se puede pagar un compromiso CANCELADO");
        }
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO) {
            if (pagoId != null && pagoId.equals(compromiso.getPagoId())) {
                return CompromisoResponseDTO.desde(compromiso);
            }
            throw new RecursoDuplicadoException("El compromiso ya se encuentra PAGADO");
        }
        if (pagoId != null && pagoId <= 0) {
            throw new SolicitudInvalidaException("El identificador del pago debe ser positivo");
        }
        compromiso.setPagoId(pagoId);
        compromiso.setEstado(EstadoCompromiso.PAGADO);
        return CompromisoResponseDTO.desde(compromisoRepository.save(compromiso));
    }

    public CompromisoResponseDTO cancelar(Long id) {
        Compromiso compromiso = obtenerCompromiso(id);
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO) {
            throw new RecursoDuplicadoException("No se puede cancelar un compromiso PAGADO");
        }
        if (compromiso.getEstado() == EstadoCompromiso.CANCELADO) {
            throw new RecursoDuplicadoException("El compromiso ya se encuentra CANCELADO");
        }
        compromiso.setEstado(EstadoCompromiso.CANCELADO);
        return CompromisoResponseDTO.desde(compromisoRepository.save(compromiso));
    }

    public void eliminar(Long id) {
        compromisoRepository.delete(obtenerCompromiso(id));
    }

    public int actualizarVencidos() {
        List<Compromiso> vencidos = compromisoRepository.buscarVencidos(LocalDate.now());
        for (Compromiso compromiso : vencidos) {
            compromiso.setEstado(EstadoCompromiso.VENCIDO);
        }
        compromisoRepository.saveAll(vencidos);
        return vencidos.size();
    }

    public int detectarCompromisosVencidos() {
        return actualizarVencidos();
    }

    private Compromiso obtenerCompromiso(Long id) {
        return compromisoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));
    }

    private void validarRequest(CompromisoRequestDTO request) {
        if (request == null) {
            throw new SolicitudInvalidaException("Los datos del compromiso son obligatorios");
        }
        if (request.getUsuarioId() == null || request.getUsuarioId() <= 0
                || request.getEmpresaId() == null || request.getEmpresaId() <= 0) {
            throw new SolicitudInvalidaException("Los identificadores de usuario y empresa deben ser positivos");
        }
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new SolicitudInvalidaException("La descripción es obligatoria");
        }
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudInvalidaException("El monto debe ser mayor que cero");
        }
        if (request.getFechaVencimiento() == null) {
            throw new SolicitudInvalidaException("La fecha de vencimiento es obligatoria");
        }
        if (request.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new SolicitudInvalidaException("La fecha de vencimiento no puede ser anterior a la fecha actual");
        }
    }

    private void validarUsuarioActivo(Long usuarioId) {
        UsuarioClient.UsuarioExterno usuario = usuarioClient.buscarPorId(usuarioId);
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new SolicitudInvalidaException("El usuario no está activo");
        }
    }

    private void validarEmpresaActiva(Long empresaId) {
        EmpresaClient.EmpresaExterna empresa = empresaClient.buscarPorId(empresaId);
        if (!Boolean.TRUE.equals(empresa.activo())) {
            throw new SolicitudInvalidaException("La empresa no está activa");
        }
    }

    private void exigirPrivilegiado(AccesoPropietario acceso) {
        if (!acceso.privilegiado()) {
            throw new AccesoNoAutorizadoException();
        }
    }

    private void aplicarDatos(Compromiso compromiso, CompromisoRequestDTO request) {
        compromiso.setUsuarioId(request.getUsuarioId());
        compromiso.setEmpresaId(request.getEmpresaId());
        compromiso.setDescripcion(request.getDescripcion());
        compromiso.setMonto(request.getMonto());
        compromiso.setFechaVencimiento(request.getFechaVencimiento());
        compromiso.setObservaciones(request.getObservaciones());
    }
}
