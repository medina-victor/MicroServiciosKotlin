package cl.pagoflex.pagos.service;

import cl.pagoflex.pagos.dto.PagoRequestDTO;
import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.exception.PagoDuplicadoException;
import cl.pagoflex.pagos.exception.PagoNotFoundException;
import cl.pagoflex.pagos.exception.PagoOperacionException;
import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.MetodoPago;
import cl.pagoflex.pagos.model.Pago;
import cl.pagoflex.pagos.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository repository;

    private PagoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PagoServiceImpl(repository);
    }

    @Test
    void registraPagoPendienteConReferenciaUnica() {
        when(repository.existsByCompromisoIdAndEstado(100L, EstadoPago.EXITOSO)).thenReturn(false);
        when(repository.existsByReferenciaTransaccion(any())).thenReturn(false);
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            pago.setId(1L);
            return pago;
        });

        PagoResponseDTO response = service.registrar(request());

        assertThat(response.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(response.getReferenciaTransaccion()).startsWith("PAG-");
        assertThat(response.getFechaPago()).isNotNull();
        verify(repository).save(any(Pago.class));
    }

    @Test
    void noRegistraCompromisoConPagoExitoso() {
        when(repository.existsByCompromisoIdAndEstado(100L, EstadoPago.EXITOSO)).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(request()))
                .isInstanceOf(PagoDuplicadoException.class);
        verify(repository, never()).save(any(Pago.class));
    }

    @Test
    void buscaPagoYListados() {
        Pago pago = pago(EstadoPago.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(pago));
        when(repository.findAll()).thenReturn(List.of(pago));
        when(repository.findByUsuarioId(10L)).thenReturn(List.of(pago));
        when(repository.findByEmpresaId(20L)).thenReturn(List.of(pago));
        when(repository.findByCompromisoId(100L)).thenReturn(List.of(pago));
        when(repository.findByEstado(EstadoPago.PENDIENTE)).thenReturn(List.of(pago));

        assertThat(service.buscarPorId(1L).getId()).isEqualTo(1L);
        assertThat(service.listarTodos()).hasSize(1);
        assertThat(service.buscarPorUsuario(10L)).hasSize(1);
        assertThat(service.buscarPorEmpresa(20L)).hasSize(1);
        assertThat(service.buscarPorCompromiso(100L)).hasSize(1);
        assertThat(service.buscarPorEstado(EstadoPago.PENDIENTE)).hasSize(1);
    }

    @Test
    void manejaPagoInexistente() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L)).isInstanceOf(PagoNotFoundException.class);
    }

    @Test
    void permiteTransicionesDesdePendiente() {
        Pago exitoso = pago(EstadoPago.PENDIENTE);
        Pago rechazado = pago(EstadoPago.PENDIENTE);
        Pago anulado = pago(EstadoPago.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(exitoso));
        when(repository.findById(2L)).thenReturn(Optional.of(rechazado));
        when(repository.findById(3L)).thenReturn(Optional.of(anulado));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.marcarExitoso(1L).getEstado()).isEqualTo(EstadoPago.EXITOSO);
        assertThat(service.rechazar(2L).getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(service.anular(3L).getEstado()).isEqualTo(EstadoPago.ANULADO);
    }

    @Test
    void noPermiteUnSegundoPagoExitosoParaElCompromiso() {
        Pago pendiente = pago(EstadoPago.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(pendiente));
        when(repository.existsByCompromisoIdAndEstado(100L, EstadoPago.EXITOSO)).thenReturn(true);

        assertThatThrownBy(() -> service.marcarExitoso(1L)).isInstanceOf(PagoDuplicadoException.class);
        verify(repository, never()).save(any(Pago.class));
    }

    @Test
    void impideTransicionesNoPermitidasYEliminarExitoso() {
        Pago rechazado = pago(EstadoPago.RECHAZADO);
        Pago anulado = pago(EstadoPago.ANULADO);
        Pago exitoso = pago(EstadoPago.EXITOSO);
        when(repository.findById(1L)).thenReturn(Optional.of(rechazado));
        when(repository.findById(2L)).thenReturn(Optional.of(anulado));
        when(repository.findById(3L)).thenReturn(Optional.of(exitoso));

        assertThatThrownBy(() -> service.marcarExitoso(1L)).isInstanceOf(PagoOperacionException.class);
        assertThatThrownBy(() -> service.marcarExitoso(2L)).isInstanceOf(PagoOperacionException.class);
        assertThatThrownBy(() -> service.eliminar(3L)).isInstanceOf(PagoOperacionException.class);
        verify(repository, never()).delete(exitoso);
    }

    @Test
    void eliminaPagoNoExitoso() {
        Pago pendiente = pago(EstadoPago.PENDIENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(pendiente));

        service.eliminar(1L);

        verify(repository).delete(pendiente);
    }

    private PagoRequestDTO request() {
        PagoRequestDTO request = new PagoRequestDTO();
        request.setCompromisoId(100L);
        request.setUsuarioId(10L);
        request.setEmpresaId(20L);
        request.setMonto(new BigDecimal("1250.00"));
        request.setMetodoPago(MetodoPago.TRANSFERENCIA);
        request.setDescripcion("Pago de prueba");
        return request;
    }

    private Pago pago(EstadoPago estado) {
        Pago pago = new Pago();
        pago.setId(1L);
        pago.setCompromisoId(100L);
        pago.setUsuarioId(10L);
        pago.setEmpresaId(20L);
        pago.setMonto(new BigDecimal("1250.00"));
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setEstado(estado);
        pago.setReferenciaTransaccion("PAG-test");
        return pago;
    }
}
