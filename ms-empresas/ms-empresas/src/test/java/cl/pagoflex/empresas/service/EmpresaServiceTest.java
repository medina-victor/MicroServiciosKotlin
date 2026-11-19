package cl.pagoflex.empresas.service;

import cl.pagoflex.empresas.dto.EmpresaRequestDTO;
import cl.pagoflex.empresas.dto.EmpresaResponseDTO;
import cl.pagoflex.empresas.exception.RecursoDuplicadoException;
import cl.pagoflex.empresas.exception.RecursoNoEncontradoException;
import cl.pagoflex.empresas.model.Empresa;
import cl.pagoflex.empresas.repository.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    @Test
    void registraUnaEmpresa() {
        EmpresaRequestDTO request = solicitud("76.123.456-7");
        when(empresaRepository.existsByRut(request.getRut())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresa = invocation.getArgument(0);
            empresa.setId(1L);
            return empresa;
        });

        EmpresaResponseDTO response = empresaService.registrar(request);

        assertEquals(1L, response.getId());
        assertEquals("76.123.456-7", response.getRut());
        assertTrue(response.getActivo());
    }

    @Test
    void rechazaRutDuplicadoAlRegistrar() {
        EmpresaRequestDTO request = solicitud("76.123.456-7");
        when(empresaRepository.existsByRut(request.getRut())).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> empresaService.registrar(request));
    }

    @Test
    void consultaTodasLasEmpresas() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa(1L, "76.123.456-7")));

        assertEquals(1, empresaService.listarTodas().size());
    }

    @Test
    void buscaPorId() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa(1L, "76.123.456-7")));

        assertEquals(1L, empresaService.buscarPorId(1L).getId());
    }

    @Test
    void manejaEmpresaInexistente() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> empresaService.buscarPorId(99L));
    }

    @Test
    void buscaPorRut() {
        when(empresaRepository.findByRut("76.123.456-7"))
                .thenReturn(Optional.of(empresa(1L, "76.123.456-7")));

        assertEquals(1L, empresaService.buscarPorRut("76.123.456-7").getId());
    }

    @Test
    void actualizaDatosSinCambiarEstado() {
        Empresa empresa = empresa(1L, "76.123.456-7");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByRutAndIdNot("76.123.456-7", 1L)).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmpresaResponseDTO response = empresaService.actualizar(1L, solicitud("76.123.456-7"));

        assertEquals("Comercial Demo SpA", response.getRazonSocial());
        assertTrue(response.getActivo());
    }

    @Test
    void rechazaRutDeOtraEmpresaAlActualizar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa(1L, "76.123.456-7")));
        when(empresaRepository.existsByRutAndIdNot("77.123.456-8", 1L)).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class,
                () -> empresaService.actualizar(1L, solicitud("77.123.456-8")));
    }

    @Test
    void eliminaEmpresa() {
        Empresa empresa = empresa(1L, "76.123.456-7");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        empresaService.eliminar(1L);

        verify(empresaRepository).delete(empresa);
    }

    @Test
    void cambiaEstadoDeEmpresa() {
        Empresa empresa = empresa(1L, "76.123.456-7");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertFalse(empresaService.desactivar(1L).getActivo());
        assertTrue(empresaService.activar(1L).getActivo());
    }

    private EmpresaRequestDTO solicitud(String rut) {
        return new EmpresaRequestDTO("Comercial Demo SpA", "Demo", rut,
                "contacto@demo.cl", "+56 9 1234 5678", "Av. Providencia 123", "Empresa de prueba");
    }

    private Empresa empresa(Long id, String rut) {
        Empresa empresa = new Empresa("Comercial Demo SpA", "Demo", rut,
                "contacto@demo.cl", "+56 9 1234 5678", "Av. Providencia 123", "Empresa de prueba");
        empresa.setId(id);
        return empresa;
    }
}
