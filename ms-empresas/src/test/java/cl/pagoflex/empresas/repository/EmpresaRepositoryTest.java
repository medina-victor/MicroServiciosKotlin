package cl.pagoflex.empresas.repository;

import cl.pagoflex.empresas.model.Empresa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Test
    void guardaEmpresaYGeneraIdYFecha() {
        Empresa guardada = empresaRepository.saveAndFlush(empresa("76.123.456-7"));

        assertNotNull(guardada.getId());
        assertNotNull(guardada.getFechaRegistro());
        assertTrue(guardada.getActivo());
    }

    @Test
    void buscaPorId() {
        Empresa guardada = empresaRepository.saveAndFlush(empresa("76.123.456-7"));

        assertTrue(empresaRepository.findById(guardada.getId()).isPresent());
    }

    @Test
    void buscaPorRut() {
        empresaRepository.saveAndFlush(empresa("76.123.456-7"));

        assertEquals("Demo", empresaRepository.findByRut("76.123.456-7").orElseThrow().getNombreComercial());
        assertTrue(empresaRepository.existsByRut("76.123.456-7"));
    }

    @Test
    void laBaseDeDatosImpideRutsDuplicados() {
        empresaRepository.saveAndFlush(empresa("76.123.456-7"));

        assertThrows(DataIntegrityViolationException.class,
                () -> empresaRepository.saveAndFlush(empresa("76.123.456-7")));
    }

    private Empresa empresa(String rut) {
        return new Empresa("Comercial Demo SpA", "Demo", rut,
                "contacto@demo.cl", "+56 9 1234 5678", "Av. Providencia 123", null);
    }
}
