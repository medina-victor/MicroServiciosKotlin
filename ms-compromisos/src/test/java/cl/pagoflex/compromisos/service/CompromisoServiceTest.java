package cl.pagoflex.compromisos.service;

import cl.pagoflex.compromisos.client.EmpresaClient;
import cl.pagoflex.compromisos.client.UsuarioClient;
import cl.pagoflex.compromisos.dto.CompromisoRequestDTO;
import cl.pagoflex.compromisos.dto.CompromisoResponseDTO;
import cl.pagoflex.compromisos.exception.RecursoNoEncontradoException;
import cl.pagoflex.compromisos.exception.RecursoDuplicadoException;
import cl.pagoflex.compromisos.exception.SolicitudInvalidaException;
import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import cl.pagoflex.compromisos.repository.CompromisoRepository;
import cl.pagoflex.compromisos.security.AccesoNoAutorizadoException;
import cl.pagoflex.compromisos.security.AccesoPropietario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompromisoServiceTest {

    @Mock
    private CompromisoRepository repository;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private EmpresaClient empresaClient;

    private CompromisoService service;

    @BeforeEach
    void setUp() {
        service = new CompromisoService(repository, usuarioClient, empresaClient);
    }

    @Test
    void registraYGuardaCompromisoPendiente() {
        CompromisoRequestDTO request = request();
        Compromiso guardado = from(request);
        guardado.setId(1L);
        when(usuarioClient.buscarPorId(10L)).thenReturn(new UsuarioClient.UsuarioExterno(10L, true));
        when(empresaClient.buscarPorId(20L)).thenReturn(new EmpresaClient.EmpresaExterna(20L, true));
        when(repository.save(any(Compromiso.class))).thenReturn(guardado);

        CompromisoResponseDTO response = service.registrar(request(), AccesoPropietario.admin());

        assertThat(response.getEstado()).isEqualTo(EstadoCompromiso.PENDIENTE);
        verify(repository).save(any(Compromiso.class));
    }

    @Test
    void validaUsuarioActivoAntesDeGuardar() {
        when(usuarioClient.buscarPorId(10L)).thenReturn(new UsuarioClient.UsuarioExterno(10L, false));
        assertThatThrownBy(() -> service.registrar(request(), AccesoPropietario.admin()))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("usuario no está activo");
        verify(repository, org.mockito.Mockito.never()).save(any(Compromiso.class));
    }

    @Test
    void rechazaMontoInvalido() {
        CompromisoRequestDTO request = request();
        request.setMonto(BigDecimal.ZERO);
        assertThatThrownBy(() -> service.registrar(request, AccesoPropietario.admin()))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void listaBuscaYFiltra() {
        Compromiso compromiso = from(request());
        compromiso.setId(1L);
        when(repository.findAll()).thenReturn(List.of(compromiso));
        when(repository.buscarVencidos(any(LocalDate.class))).thenReturn(List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(compromiso));
        when(repository.findByUsuarioId(10L)).thenReturn(List.of(compromiso));
        when(repository.findByEmpresaId(20L)).thenReturn(List.of(compromiso));
        when(repository.findByEstado(EstadoCompromiso.PENDIENTE)).thenReturn(List.of(compromiso));

        AccesoPropietario admin = AccesoPropietario.admin();
        assertThat(service.listarTodas(admin)).hasSize(1);
        assertThat(service.buscarPorId(1L, admin).getId()).isEqualTo(1L);
        assertThat(service.buscarPorUsuario(10L, admin)).hasSize(1);
        assertThat(service.buscarPorEmpresa(20L, admin)).hasSize(1);
        assertThat(service.buscarPorEstado(EstadoCompromiso.PENDIENTE, admin)).hasSize(1);
    }

    @Test
    void manejoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L, AccesoPropietario.admin()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizaYMarcaPagado() {
        Compromiso compromiso = from(request());
        compromiso.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(compromiso));
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(compromiso));
        when(repository.save(any(Compromiso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.actualizar(1L, request(), AccesoPropietario.admin())
                .getEstado()).isEqualTo(EstadoCompromiso.PENDIENTE);
        assertThat(service.pagar(1L).getEstado()).isEqualTo(EstadoCompromiso.PAGADO);
    }

    @Test
    void rechazaClienteQueIntentaConsultarCompromisoAjeno() {
        Compromiso ajeno = from(request());
        ajeno.setId(1L);
        when(repository.buscarVencidos(any(LocalDate.class))).thenReturn(List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> service.buscarPorId(1L,
                new AccesoPropietario(false, "CLIENTE", 11L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void rechazaEmpresaQueIntentaConsultarCompromisoDeOtraEmpresa() {
        Compromiso ajeno = from(request());
        ajeno.setId(1L);
        when(repository.buscarVencidos(any(LocalDate.class))).thenReturn(List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> service.buscarPorId(1L,
                new AccesoPropietario(false, "EMPRESA", 99L, 21L)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void rechazaActualizarCompromisoAjenoAunqueElBodyIndiqueAlUsuarioAutenticado() {
        Compromiso ajeno = from(request());
        ajeno.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> service.actualizar(1L, request(),
                new AccesoPropietario(false, "CLIENTE", 11L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void rechazaQueClienteCambieElPropietarioDesdeElBody() {
        Compromiso propio = from(request());
        propio.setId(1L);
        CompromisoRequestDTO solicitud = request();
        solicitud.setUsuarioId(11L);
        when(repository.findById(1L)).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> service.actualizar(1L, solicitud,
                new AccesoPropietario(false, "CLIENTE", 10L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void clienteNoPuedeListarTodosLosCompromisos() {
        assertThatThrownBy(() -> service.listarTodas(
                new AccesoPropietario(false, "CLIENTE", 10L, null)))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void elMismoPagoPuedeReintentarLaConfirmacionDeFormaIdempotente() {
        Compromiso compromiso = from(request());
        compromiso.setId(1L);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(compromiso));
        when(repository.save(any(Compromiso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.pagar(1L, 77L).getEstado()).isEqualTo(EstadoCompromiso.PAGADO);
        assertThat(service.pagar(1L, 77L).getEstado()).isEqualTo(EstadoCompromiso.PAGADO);
        assertThat(compromiso.getPagoId()).isEqualTo(77L);
    }

    @Test
    void cancelarYDetectarVencidos() {
        Compromiso vencido = from(request());
        vencido.setId(1L);
        vencido.setFechaVencimiento(LocalDate.now().minusDays(1));
        when(repository.buscarVencidos(any(LocalDate.class))).thenReturn(List.of(vencido));
        when(repository.saveAll(List.of(vencido))).thenReturn(List.of(vencido));

        assertThat(service.actualizarVencidos()).isEqualTo(1);
        assertThat(vencido.getEstado()).isEqualTo(EstadoCompromiso.VENCIDO);
    }

    @Test
    void impideTransicionesInvalidas() {
        Compromiso cancelado = from(request());
        cancelado.setEstado(EstadoCompromiso.CANCELADO);
        Compromiso pagado = from(request());
        pagado.setEstado(EstadoCompromiso.PAGADO);
        when(repository.findByIdForUpdate(1L)).thenReturn(Optional.of(cancelado));
        when(repository.findById(2L)).thenReturn(Optional.of(pagado));

        assertThatThrownBy(() -> service.pagar(1L)).isInstanceOf(RecursoDuplicadoException.class);
        assertThatThrownBy(() -> service.cancelar(2L)).isInstanceOf(RecursoDuplicadoException.class);
    }

    private CompromisoRequestDTO request() {
        CompromisoRequestDTO request = new CompromisoRequestDTO();
        request.setUsuarioId(10L);
        request.setEmpresaId(20L);
        request.setDescripcion("Cuota");
        request.setMonto(new BigDecimal("1000"));
        request.setFechaVencimiento(LocalDate.now().plusDays(10));
        return request;
    }

    private Compromiso from(CompromisoRequestDTO request) {
        Compromiso compromiso = new Compromiso();
        compromiso.setUsuarioId(request.getUsuarioId());
        compromiso.setEmpresaId(request.getEmpresaId());
        compromiso.setDescripcion(request.getDescripcion());
        compromiso.setMonto(request.getMonto());
        compromiso.setFechaVencimiento(request.getFechaVencimiento());
        compromiso.setEstado(EstadoCompromiso.PENDIENTE);
        return compromiso;
    }
}
