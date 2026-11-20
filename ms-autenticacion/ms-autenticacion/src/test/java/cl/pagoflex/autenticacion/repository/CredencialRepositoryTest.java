package cl.pagoflex.autenticacion.repository;

import cl.pagoflex.autenticacion.model.Credencial;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CredencialRepositoryTest {
    @Autowired
    private CredencialRepository repository;

    @Test
    void guardaBuscaYDesactivaCredencial() {
        Credencial credencial = credencial(1L, "cliente@pagoflex.cl");
        Credencial guardada = repository.saveAndFlush(credencial);

        assertThat(repository.findByCorreo("cliente@pagoflex.cl")).isPresent();
        assertThat(repository.findByUsuarioId(1L)).isPresent();
        assertThat(repository.existsByCorreo("cliente@pagoflex.cl")).isTrue();
        assertThat(repository.existsByUsuarioId(1L)).isTrue();

        guardada.setActivo(false);
        repository.saveAndFlush(guardada);
        assertThat(repository.findByUsuarioId(1L).orElseThrow().isActivo()).isFalse();
    }

    @Test
    void impideCorreoODuplicadoUsuario() {
        repository.saveAndFlush(credencial(1L, "unico@pagoflex.cl"));

        assertThatThrownBy(() -> repository.saveAndFlush(credencial(2L, "unico@pagoflex.cl")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> repository.saveAndFlush(credencial(1L, "otro@pagoflex.cl")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Credencial credencial(Long usuarioId, String correo) {
        Credencial credencial = new Credencial();
        credencial.setUsuarioId(usuarioId);
        credencial.setCorreo(correo);
        credencial.setPasswordHash("$2a$10$hashPrueba");
        credencial.setActivo(true);
        return credencial;
    }
}
