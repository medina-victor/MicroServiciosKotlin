package cl.pagoflex.usuarios.service;

import cl.pagoflex.usuarios.dto.UsuarioRequest;
import cl.pagoflex.usuarios.dto.UsuarioResponse;
import cl.pagoflex.usuarios.dto.UsuarioRolRequest;
import cl.pagoflex.usuarios.exception.CorreoDuplicadoException;
import cl.pagoflex.usuarios.exception.SolicitudInvalidaException;
import cl.pagoflex.usuarios.exception.UsuarioNoEncontradoException;
import cl.pagoflex.usuarios.model.RolUsuario;
import cl.pagoflex.usuarios.model.Usuario;
import cl.pagoflex.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new CorreoDuplicadoException();
        }

        Usuario usuario = new Usuario(request.getNombre(), request.getCorreo(),
                request.getTelefono(), RolUsuario.CLIENTE, null);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.desde(obtenerUsuario(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorCorreo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsuarioNoEncontradoException(correo));
        return UsuarioResponse.desde(usuario);
    }

    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = obtenerUsuario(id);
        if (usuarioRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
            throw new CorreoDuplicadoException();
        }

        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setTelefono(request.getTelefono());
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse desactivarUsuario(Long id) {
        Usuario usuario = obtenerUsuario(id);
        usuario.setActivo(false);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    public UsuarioResponse cambiarRol(Long id, UsuarioRolRequest request) {
        Usuario usuario = obtenerUsuario(id);
        if (request.getRol() == RolUsuario.EMPRESA
                && request.getEmpresaId() == null
                && usuario.getEmpresaId() == null) {
            throw new SolicitudInvalidaException("empresaId es obligatorio para asignar el rol EMPRESA");
        }

        usuario.setRol(request.getRol());
        if (request.getEmpresaId() != null) {
            usuario.setEmpresaId(request.getEmpresaId());
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarPorRol(RolUsuario rol) {
        return usuarioRepository.findByRol(rol).stream().map(UsuarioResponse::desde).toList();
    }

    private Usuario obtenerUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }
}
