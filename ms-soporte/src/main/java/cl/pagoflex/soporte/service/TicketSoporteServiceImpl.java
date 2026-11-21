package cl.pagoflex.soporte.service;

import cl.pagoflex.soporte.client.EmpresaClient;
import cl.pagoflex.soporte.client.UsuarioClient;
import cl.pagoflex.soporte.dto.TicketEstadoDTO;
import cl.pagoflex.soporte.dto.TicketRespuestaDTO;
import cl.pagoflex.soporte.dto.TicketSoporteRequestDTO;
import cl.pagoflex.soporte.dto.TicketSoporteResponseDTO;
import cl.pagoflex.soporte.exception.TicketNoEncontradoException;
import cl.pagoflex.soporte.exception.TicketOperacionException;
import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.model.TicketSoporte;
import cl.pagoflex.soporte.repository.TicketSoporteRepository;
import cl.pagoflex.soporte.security.AccesoNoAutorizadoException;
import cl.pagoflex.soporte.security.AccesoPropietario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TicketSoporteServiceImpl implements TicketSoporteService {

    private final TicketSoporteRepository repository;
    private final UsuarioClient usuarioClient;
    private final EmpresaClient empresaClient;

    public TicketSoporteServiceImpl(TicketSoporteRepository repository,
                                    UsuarioClient usuarioClient, EmpresaClient empresaClient) {
        this.repository = repository;
        this.usuarioClient = usuarioClient;
        this.empresaClient = empresaClient;
    }

    @Override
    public TicketSoporteResponseDTO crear(TicketSoporteRequestDTO request, AccesoPropietario acceso) {
        validarCreacion(request);
        if ((acceso.esCliente() && !acceso.usuarioId().equals(request.getUsuarioId()))
                || (acceso.esEmpresa() && !acceso.empresaId().equals(request.getEmpresaId()))) {
            throw new AccesoNoAutorizadoException();
        }
        UsuarioClient.UsuarioExterno usuario = usuarioClient.buscarPorId(request.getUsuarioId());
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new TicketOperacionException("El usuario no está activo");
        }
        if (request.getEmpresaId() != null) {
            EmpresaClient.EmpresaExterna empresa = empresaClient.buscarPorId(request.getEmpresaId());
            if (!Boolean.TRUE.equals(empresa.activo())) {
                throw new TicketOperacionException("La empresa no está activa");
            }
        }
        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setEmpresaId(request.getEmpresaId());
        ticket.setAsunto(request.getAsunto().trim());
        ticket.setDescripcion(request.getDescripcion().trim());
        ticket.setCategoria(request.getCategoria());
        ticket.setPrioridad(request.getPrioridad());
        ticket.setEstado(EstadoTicket.ABIERTO);
        return TicketSoporteResponseDTO.desde(repository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> listarTodas(AccesoPropietario acceso) {
        exigirPrivilegiado(acceso);
        return repository.findAll().stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketSoporteResponseDTO buscarPorId(Long id, AccesoPropietario acceso) {
        TicketSoporte ticket = buscarEntidad(id);
        acceso.exigirPropietario(ticket.getUsuarioId(), ticket.getEmpresaId());
        return TicketSoporteResponseDTO.desde(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> buscarPorUsuario(Long usuarioId, AccesoPropietario acceso) {
        if (acceso.esCliente() && !acceso.usuarioId().equals(usuarioId)) {
            throw new AccesoNoAutorizadoException();
        }
        validarReferencia(usuarioId, "usuario");
        List<TicketSoporte> tickets = acceso.esEmpresa()
                ? repository.findByUsuarioIdAndEmpresaId(usuarioId, acceso.empresaId())
                : repository.findByUsuarioId(usuarioId);
        return tickets.stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> buscarPorEmpresa(Long empresaId, AccesoPropietario acceso) {
        if (acceso.esEmpresa() && !acceso.empresaId().equals(empresaId)) {
            throw new AccesoNoAutorizadoException();
        }
        validarReferencia(empresaId, "empresa");
        List<TicketSoporte> tickets = acceso.esCliente()
                ? repository.findByUsuarioIdAndEmpresaId(acceso.usuarioId(), empresaId)
                : repository.findByEmpresaId(empresaId);
        return tickets.stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> buscarPorEstado(EstadoTicket estado, AccesoPropietario acceso) {
        if (estado == null) throw new TicketOperacionException("El estado es obligatorio");
        List<TicketSoporte> tickets = acceso.esCliente()
                ? repository.findByUsuarioIdAndEstado(acceso.usuarioId(), estado)
                : acceso.esEmpresa()
                    ? repository.findByEmpresaIdAndEstado(acceso.empresaId(), estado)
                    : repository.findByEstado(estado);
        return tickets.stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> buscarPorCategoria(CategoriaSoporte categoria, AccesoPropietario acceso) {
        if (categoria == null) throw new TicketOperacionException("La categoría es obligatoria");
        List<TicketSoporte> tickets = acceso.esCliente()
                ? repository.findByUsuarioIdAndCategoria(acceso.usuarioId(), categoria)
                : acceso.esEmpresa()
                    ? repository.findByEmpresaIdAndCategoria(acceso.empresaId(), categoria)
                    : repository.findByCategoria(categoria);
        return tickets.stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSoporteResponseDTO> buscarPorPrioridad(PrioridadTicket prioridad, AccesoPropietario acceso) {
        if (prioridad == null) throw new TicketOperacionException("La prioridad es obligatoria");
        List<TicketSoporte> tickets = acceso.esCliente()
                ? repository.findByUsuarioIdAndPrioridad(acceso.usuarioId(), prioridad)
                : acceso.esEmpresa()
                    ? repository.findByEmpresaIdAndPrioridad(acceso.empresaId(), prioridad)
                    : repository.findByPrioridad(prioridad);
        return tickets.stream().map(TicketSoporteResponseDTO::desde).toList();
    }

    @Override
    public TicketSoporteResponseDTO actualizar(
            Long id, TicketSoporteRequestDTO request, AccesoPropietario acceso) {
        TicketSoporte ticket = buscarEntidad(id);
        acceso.exigirPropietario(ticket.getUsuarioId(), ticket.getEmpresaId());
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("No se puede actualizar un ticket cerrado");
        }
        validarCreacion(request);
        if (!ticket.getUsuarioId().equals(request.getUsuarioId())) {
            validarUsuarioActivo(request.getUsuarioId());
        }
        if (!java.util.Objects.equals(ticket.getEmpresaId(), request.getEmpresaId())
                && request.getEmpresaId() != null) {
            validarEmpresaActiva(request.getEmpresaId());
        }
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setEmpresaId(request.getEmpresaId());
        ticket.setAsunto(request.getAsunto().trim());
        ticket.setDescripcion(request.getDescripcion().trim());
        ticket.setCategoria(request.getCategoria());
        ticket.setPrioridad(request.getPrioridad());
        return TicketSoporteResponseDTO.desde(repository.save(ticket));
    }

    @Override
    public TicketSoporteResponseDTO cambiarEstado(Long id, TicketEstadoDTO dto) {
        TicketSoporte ticket = buscarEntidad(id);
        if (dto == null || dto.getEstado() == null) throw new TicketOperacionException("El estado es obligatorio");
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("No se puede cambiar el estado de un ticket cerrado");
        }
        if (dto.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("Utilice el endpoint de cierre para cerrar el ticket");
        }
        ticket.setEstado(dto.getEstado());
        return TicketSoporteResponseDTO.desde(repository.save(ticket));
    }

    @Override
    public TicketSoporteResponseDTO responder(Long id, TicketRespuestaDTO dto) {
        TicketSoporte ticket = buscarEntidad(id);
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("No se puede responder un ticket cerrado");
        }
        if (dto == null || dto.getRespuesta() == null || dto.getRespuesta().isBlank()) {
            throw new TicketOperacionException("La respuesta es obligatoria");
        }
        if (dto.getRespuesta().length() > 2000) throw new TicketOperacionException("La respuesta no puede superar 2000 caracteres");
        ticket.setRespuesta(dto.getRespuesta().trim());
        ticket.setEstado(EstadoTicket.RESPONDIDO);
        return TicketSoporteResponseDTO.desde(repository.save(ticket));
    }

    @Override
    public TicketSoporteResponseDTO cerrar(Long id) {
        TicketSoporte ticket = buscarEntidad(id);
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("El ticket ya se encuentra cerrado");
        }
        ticket.setEstado(EstadoTicket.CERRADO);
        ticket.setFechaCierre(LocalDateTime.now());
        return TicketSoporteResponseDTO.desde(repository.save(ticket));
    }

    @Override
    public void eliminar(Long id) {
        TicketSoporte ticket = buscarEntidad(id);
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new TicketOperacionException("No se puede eliminar un ticket cerrado porque perdería su historial");
        }
        repository.delete(ticket);
    }

    private TicketSoporte buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TicketNoEncontradoException(id));
    }

    private void validarCreacion(TicketSoporteRequestDTO request) {
        if (request == null) throw new TicketOperacionException("La solicitud es obligatoria");
        validarReferencia(request.getUsuarioId(), "usuario");
        if (request.getEmpresaId() != null && request.getEmpresaId() <= 0) {
            throw new TicketOperacionException("El identificador de la empresa debe ser mayor que cero");
        }
        if (request.getAsunto() == null || request.getAsunto().isBlank()) {
            throw new TicketOperacionException("El asunto es obligatorio");
        }
        if (request.getAsunto().length() > 150) throw new TicketOperacionException("El asunto no puede superar 150 caracteres");
        if (request.getDescripcion() == null || request.getDescripcion().isBlank()) {
            throw new TicketOperacionException("La descripción es obligatoria");
        }
        if (request.getDescripcion().length() > 1000) throw new TicketOperacionException("La descripción no puede superar 1000 caracteres");
        if (request.getCategoria() == null) throw new TicketOperacionException("La categoría es obligatoria");
    }

    private void validarReferencia(Long id, String nombre) {
        if (id == null || id <= 0) throw new TicketOperacionException("El identificador del " + nombre + " debe ser mayor que cero");
    }

    private void validarUsuarioActivo(Long usuarioId) {
        UsuarioClient.UsuarioExterno usuario = usuarioClient.buscarPorId(usuarioId);
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new TicketOperacionException("El usuario no está activo");
        }
    }

    private void validarEmpresaActiva(Long empresaId) {
        EmpresaClient.EmpresaExterna empresa = empresaClient.buscarPorId(empresaId);
        if (!Boolean.TRUE.equals(empresa.activo())) {
            throw new TicketOperacionException("La empresa no está activa");
        }
    }

    private void exigirPrivilegiado(AccesoPropietario acceso) {
        if (!acceso.privilegiado()) {
            throw new AccesoNoAutorizadoException();
        }
    }
}
