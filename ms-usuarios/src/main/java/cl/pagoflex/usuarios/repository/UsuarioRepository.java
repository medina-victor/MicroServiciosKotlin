package cl.pagoflex.usuarios.repository;

import cl.pagoflex.usuarios.model.RolUsuario;
import cl.pagoflex.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, Long id);

    List<Usuario> findByRol(RolUsuario rol);

    List<Usuario> findByActivoTrue();
}
