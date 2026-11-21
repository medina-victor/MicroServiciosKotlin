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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketSoporteServiceTest {

    @Mock
    private TicketSoporteRepository repository;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private EmpresaClient empresaClient;
    private TicketSoporteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TicketSoporteServiceImpl(repository, usuarioClient, empresaClient);
    }

    @Test
    void creaTicketAbiertoConPrioridadMedia() {
        TicketSoporte guardado = desde(request());
        guardado.setId(1L);
        when(usuarioClient.buscarPorId(1L)).thenReturn(new UsuarioClient.UsuarioExterno(1L, true));
        when(empresaClient.buscarPorId(2L)).thenReturn(new EmpresaClient.EmpresaExterna(2L, true));
        when(repository.save(any(TicketSoporte.class))).thenReturn(guardado);

        TicketSoporteResponseDTO respuesta = service.crear(request(), AccesoPropietario.admin());

        assertThat(respuesta.getEstado()).isEqualTo(EstadoTicket.ABIERTO);
        assertThat(respuesta.getPrioridad()).isEqualTo(PrioridadTicket.MEDIA);
        verify(repository).save(any(TicketSoporte.class));
    }

    @Test
    void validaUsuarioActivoAntesDeCrearTicket() {
        when(usuarioClient.buscarPorId(1L)).thenReturn(new UsuarioClient.UsuarioExterno(1L, false));
        assertThatThrownBy(() -> service.crear(request(), AccesoPropietario.admin()))
                .isInstanceOf(TicketOperacionException.class)
                .hasMessageContaining("usuario no está activo");
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any(TicketSoporte.class));
    }

    @Test
    void rechazaUsuarioInvalido() {
        TicketSoporteRequestDTO request = request();
        request.setUsuarioId(0L);
        assertThatThrownBy(() -> service.crear(request, AccesoPropietario.admin()))
                .isInstanceOf(TicketOperacionException.class);
    }

    @Test
    void buscaPorDistintosCriterios() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        when(repository.findAll()).thenReturn(List.of(ticket));
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        when(repository.findByUsuarioId(1L)).thenReturn(List.of(ticket));
        when(repository.findByEmpresaId(2L)).thenReturn(List.of(ticket));
        when(repository.findByEstado(EstadoTicket.ABIERTO)).thenReturn(List.of(ticket));

        AccesoPropietario admin = AccesoPropietario.admin();
        assertThat(service.listarTodas(admin)).hasSize(1);
        assertThat(service.buscarPorId(1L, admin).getId()).isEqualTo(1L);
        assertThat(service.buscarPorUsuario(1L, admin)).hasSize(1);
        assertThat(service.buscarPorEmpresa(2L, admin)).hasSize(1);
        assertThat(service.buscarPorEstado(EstadoTicket.ABIERTO, admin)).hasSize(1);
    }

    @Test
    void cambiaEstadoRespondeYCierra() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        when(repository.save(any(TicketSoporte.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketEstadoDTO estado = new TicketEstadoDTO();
        estado.setEstado(EstadoTicket.EN_REVISION);
        assertThat(service.cambiarEstado(1L, estado).getEstado()).isEqualTo(EstadoTicket.EN_REVISION);

        TicketRespuestaDTO respuesta = new TicketRespuestaDTO();
        respuesta.setRespuesta("Estamos revisando");
        assertThat(service.responder(1L, respuesta).getEstado()).isEqualTo(EstadoTicket.RESPONDIDO);
        assertThat(service.cerrar(1L).getEstado()).isEqualTo(EstadoTicket.CERRADO);
    }

    @Test
    void aplicaRestriccionesDeTicketsCerrados() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        ticket.setEstado(EstadoTicket.CERRADO);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.responder(1L, new TicketRespuestaDTO()))
                .isInstanceOf(TicketOperacionException.class);
        assertThatThrownBy(() -> service.cerrar(1L)).isInstanceOf(TicketOperacionException.class);
        assertThatThrownBy(() -> service.eliminar(1L)).isInstanceOf(TicketOperacionException.class);
        assertThatThrownBy(() -> service.actualizar(1L, request(), AccesoPropietario.admin()))
                .isInstanceOf(TicketOperacionException.class);
    }

    @Test
    void manejaTicketInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L, AccesoPropietario.admin()))
                .isInstanceOf(TicketNoEncontradoException.class);
    }

    @Test
    void clienteNoPuedeConsultarTicketDeOtroUsuario() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.buscarPorId(1L,
                new AccesoPropietario(false, "CLIENTE", 9L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void empresaNoPuedeConsultarTicketDeOtraEmpresa() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.buscarPorId(1L,
                new AccesoPropietario(false, "EMPRESA", 9L, 3L)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void rechazaActualizarTicketConEmpresaInexistente() {
        TicketSoporte ticket = desde(request());
        ticket.setId(1L);
        TicketSoporteRequestDTO actualizacion = request();
        actualizacion.setEmpresaId(3L);
        when(repository.findById(1L)).thenReturn(Optional.of(ticket));
        when(empresaClient.buscarPorId(3L)).thenThrow(
                new cl.pagoflex.soporte.exception.DependenciaRemotaException("ms-empresas", 404));

        assertThatThrownBy(() -> service.actualizar(1L, actualizacion, AccesoPropietario.admin()))
                .isInstanceOf(cl.pagoflex.soporte.exception.DependenciaRemotaException.class);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).save(any(TicketSoporte.class));
    }

    @Test
    void clienteNoPuedeListarTodosLosTickets() {
        assertThatThrownBy(() -> service.listarTodas(
                new AccesoPropietario(false, "CLIENTE", 1L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    private TicketSoporteRequestDTO request() {
        TicketSoporteRequestDTO request = new TicketSoporteRequestDTO();
        request.setUsuarioId(1L);
        request.setEmpresaId(2L);
        request.setAsunto("Problema con un pago");
        request.setDescripcion("El pago aparece pendiente");
        request.setCategoria(CategoriaSoporte.PAGOS);
        request.setPrioridad(PrioridadTicket.MEDIA);
        return request;
    }

    private TicketSoporte desde(TicketSoporteRequestDTO request) {
        TicketSoporte ticket = new TicketSoporte();
        ticket.setUsuarioId(request.getUsuarioId());
        ticket.setEmpresaId(request.getEmpresaId());
        ticket.setAsunto(request.getAsunto());
        ticket.setDescripcion(request.getDescripcion());
        ticket.setCategoria(request.getCategoria());
        ticket.setPrioridad(request.getPrioridad());
        ticket.setEstado(EstadoTicket.ABIERTO);
        return ticket;
    }
}
