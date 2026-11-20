package cl.pagoflex.empresas.service;

import cl.pagoflex.empresas.dto.EmpresaRequestDTO;
import cl.pagoflex.empresas.dto.EmpresaResponseDTO;
import cl.pagoflex.empresas.exception.RecursoDuplicadoException;
import cl.pagoflex.empresas.exception.RecursoNoEncontradoException;
import cl.pagoflex.empresas.model.Empresa;
import cl.pagoflex.empresas.repository.EmpresaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public EmpresaResponseDTO registrar(EmpresaRequestDTO request) {
        if (empresaRepository.existsByRut(request.getRut())) {
            throw new RecursoDuplicadoException("Ya existe una empresa registrada con ese RUT");
        }

        Empresa empresa = new Empresa(request.getRazonSocial(), request.getNombreComercial(),
                request.getRut(), request.getCorreo(), request.getTelefono(),
                request.getDireccion(), request.getDescripcion());
        return EmpresaResponseDTO.desde(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarTodas() {
        return empresaRepository.findAll().stream().map(EmpresaResponseDTO::desde).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorId(Long id) {
        return EmpresaResponseDTO.desde(obtenerEmpresa(id));
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorRut(String rut) {
        Empresa empresa = empresaRepository.findByRut(rut)
                .orElseThrow(() -> new RecursoNoEncontradoException(rut));
        return EmpresaResponseDTO.desde(empresa);
    }

    public EmpresaResponseDTO actualizar(Long id, EmpresaRequestDTO request) {
        Empresa empresa = obtenerEmpresa(id);
        if (empresaRepository.existsByRutAndIdNot(request.getRut(), id)) {
            throw new RecursoDuplicadoException("El RUT ya está registrado por otra empresa");
        }

        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setNombreComercial(request.getNombreComercial());
        empresa.setRut(request.getRut());
        empresa.setCorreo(request.getCorreo());
        empresa.setTelefono(request.getTelefono());
        empresa.setDireccion(request.getDireccion());
        empresa.setDescripcion(request.getDescripcion());
        return EmpresaResponseDTO.desde(empresaRepository.save(empresa));
    }

    public void eliminar(Long id) {
        empresaRepository.delete(obtenerEmpresa(id));
    }

    public EmpresaResponseDTO desactivar(Long id) {
        Empresa empresa = obtenerEmpresa(id);
        empresa.setActivo(false);
        return EmpresaResponseDTO.desde(empresaRepository.save(empresa));
    }

    public EmpresaResponseDTO activar(Long id) {
        Empresa empresa = obtenerEmpresa(id);
        empresa.setActivo(true);
        return EmpresaResponseDTO.desde(empresaRepository.save(empresa));
    }

    private Empresa obtenerEmpresa(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));
    }
}
