package cl.pagoflex.pagos.repository;

import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByUsuarioId(Long usuarioId);
    List<Pago> findByEmpresaId(Long empresaId);
    List<Pago> findByCompromisoId(Long compromisoId);
    List<Pago> findByEstado(EstadoPago estado);
    Optional<Pago> findByReferenciaTransaccion(String referenciaTransaccion);
    boolean existsByReferenciaTransaccion(String referenciaTransaccion);
    boolean existsByCompromisoId(Long compromisoId);
    boolean existsByCompromisoIdAndEstado(Long compromisoId, EstadoPago estado);
}
