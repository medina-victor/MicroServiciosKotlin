package cl.pagoflex.pagos.repository;

import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.Pago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByUsuarioId(Long usuarioId);
    List<Pago> findByEmpresaId(Long empresaId);
    List<Pago> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);
    List<Pago> findByCompromisoId(Long compromisoId);
    List<Pago> findByCompromisoIdAndUsuarioId(Long compromisoId, Long usuarioId);
    List<Pago> findByCompromisoIdAndEmpresaId(Long compromisoId, Long empresaId);
    List<Pago> findByEstado(EstadoPago estado);
    List<Pago> findByEstadoAndUsuarioId(EstadoPago estado, Long usuarioId);
    List<Pago> findByEstadoAndEmpresaId(EstadoPago estado, Long empresaId);
    Optional<Pago> findByReferenciaTransaccion(String referenciaTransaccion);
    boolean existsByReferenciaTransaccion(String referenciaTransaccion);
    boolean existsByCompromisoId(Long compromisoId);
    boolean existsByCompromisoIdAndEstado(Long compromisoId, EstadoPago estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pago p WHERE p.id = :id")
    Optional<Pago> findByIdForUpdate(@Param("id") Long id);
}
