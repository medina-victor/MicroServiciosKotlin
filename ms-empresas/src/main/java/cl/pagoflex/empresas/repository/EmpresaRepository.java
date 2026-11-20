package cl.pagoflex.empresas.repository;

import cl.pagoflex.empresas.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByRut(String rut);

    boolean existsByRut(String rut);

    boolean existsByRutAndIdNot(String rut, Long id);
}
