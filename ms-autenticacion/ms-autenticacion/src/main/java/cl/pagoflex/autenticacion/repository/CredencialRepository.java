package cl.pagoflex.autenticacion.repository;

import cl.pagoflex.autenticacion.model.Credencial;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CredencialRepository extends JpaRepository<Credencial, Long> {
    Optional<Credencial> findByCorreo(String correo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Credencial c where c.correo = :correo")
    Optional<Credencial> findByCorreoForUpdate(@Param("correo") String correo);
    Optional<Credencial> findByUsuarioId(Long usuarioId);
    boolean existsByCorreo(String correo);
    boolean existsByUsuarioId(Long usuarioId);
}
