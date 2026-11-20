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

    public CompromisoResponseDTO registrar(CompromisoRequestDTO request) {
        validarRequest(request);
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
    public List<CompromisoResponseDTO> listarTodas() {
        actualizarVencidos();
        return compromisoRepository.findAll().stream().map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public CompromisoResponseDTO buscarPorId(Long id) {
        actualizarVencidos();
        return CompromisoResponseDTO.desde(obtenerCompromiso(id));
    }

    public List<CompromisoResponseDTO> listarTodos() {
        return listarTodas();
    }

    public List<CompromisoResponseDTO> buscarPorUsuarioId(Long usuarioId) {
        return buscarPorUsuario(usuarioId);
    }

    public List<CompromisoResponseDTO> buscarPorEmpresaId(Long empresaId) {
        return buscarPorEmpresa(empresaId);
    }

    public CompromisoResponseDTO marcarPagado(Long id) {
        return pagar(id);
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorUsuario(Long usuarioId) {
        actualizarVencidos();
        return compromisoRepository.findByUsuarioId(usuarioId).stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorEmpresa(Long empresaId) {
        actualizarVencidos();
        return compromisoRepository.findByEmpresaId(empresaId).stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<CompromisoResponseDTO> buscarPorEstado(EstadoCompromiso estado) {
        actualizarVencidos();
        return compromisoRepository.findByEstado(estado).stream()
                .map(CompromisoResponseDTO::desde).toList();
    }

    public CompromisoResponseDTO actualizar(Long id, CompromisoRequestDTO request) {
        validarRequest(request);
        Compromiso compromiso = obtenerCompromiso(id);
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO
                || compromiso.getEstado() == EstadoCompromiso.CANCELADO) {
            throw new RecursoDuplicadoException("No se puede actualizar un compromiso PAGADO o CANCELADO");
        }
        aplicarDatos(compromiso, request);
        return CompromisoResponseDTO.desde(compromisoRepository.save(compromiso));
    }

    public CompromisoResponseDTO pagar(Long id) {
        Compromiso compromiso = obtenerCompromiso(id);
        if (compromiso.getEstado() == EstadoCompromiso.CANCELADO) {
            throw new RecursoDuplicadoException("No se puede pagar un compromiso CANCELADO");
        }
        if (compromiso.getEstado() == EstadoCompromiso.PAGADO) {
            throw new RecursoDuplicadoException("El compromiso ya se encuentra PAGADO");
        }
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

    public CompromisoResponseDTO marcarComoPagado(Long id) {
        return pagar(id);
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

    private void aplicarDatos(Compromiso compromiso, CompromisoRequestDTO request) {
        compromiso.setUsuarioId(request.getUsuarioId());
        compromiso.setEmpresaId(request.getEmpresaId());
        compromiso.setDescripcion(request.getDescripcion());
        compromiso.setMonto(request.getMonto());
        compromiso.setFechaVencimiento(request.getFechaVencimiento());
        compromiso.setObservaciones(request.getObservaciones());
    }
}
