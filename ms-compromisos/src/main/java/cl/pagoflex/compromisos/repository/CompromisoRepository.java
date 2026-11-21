package cl.pagoflex.compromisos.repository;

import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompromisoRepository extends JpaRepository<Compromiso, Long> {
    List<Compromiso> findByUsuarioId(Long usuarioId);
    List<Compromiso> findByEmpresaId(Long empresaId);
    List<Compromiso> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    List<Compromiso> findByEstado(EstadoCompromiso estado);
    List<Compromiso> findByEstadoAndUsuarioId(EstadoCompromiso estado, Long usuarioId);
    List<Compromiso> findByEstadoAndEmpresaId(EstadoCompromiso estado, Long empresaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compromiso c WHERE c.id = :id")
    Optional<Compromiso> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT c FROM Compromiso c WHERE c.estado = cl.pagoflex.compromisos.model.EstadoCompromiso.PENDIENTE "
            + "AND c.fechaVencimiento < :fecha")
    List<Compromiso> buscarVencidos(@Param("fecha") LocalDate fecha);

    List<Compromiso> findByFechaVencimientoBeforeAndEstado(LocalDate fecha, EstadoCompromiso estado);
}
