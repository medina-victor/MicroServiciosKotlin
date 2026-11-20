package cl.pagoflex.compromisos.repository;

import cl.pagoflex.compromisos.model.Compromiso;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CompromisoRepository extends JpaRepository<Compromiso, Long> {
    List<Compromiso> findByUsuarioId(Long usuarioId);
    List<Compromiso> findByEmpresaId(Long empresaId);
    List<Compromiso> findByEstado(EstadoCompromiso estado);

    @Query("SELECT c FROM Compromiso c WHERE c.estado = cl.pagoflex.compromisos.model.EstadoCompromiso.PENDIENTE "
            + "AND c.fechaVencimiento < :fecha")
    List<Compromiso> buscarVencidos(@Param("fecha") LocalDate fecha);

    List<Compromiso> findByFechaVencimientoBeforeAndEstado(LocalDate fecha, EstadoCompromiso estado);
}
