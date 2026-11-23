package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.client.UsuarioCliente;
import cl.pagoflex.autenticacion.dto.AuthResponseDTO;
import cl.pagoflex.autenticacion.dto.LoginRequestDTO;
import cl.pagoflex.autenticacion.dto.RegistroRequestDTO;
import cl.pagoflex.autenticacion.dto.UsuarioAutenticadoDTO;
import cl.pagoflex.autenticacion.exception.CredencialDuplicadaException;
import cl.pagoflex.autenticacion.exception.CredencialNotFoundException;
import cl.pagoflex.autenticacion.exception.CredencialesInvalidasException;
import cl.pagoflex.autenticacion.exception.ServicioUsuariosNoDisponibleException;
import cl.pagoflex.autenticacion.exception.UsuarioInactivoException;
import cl.pagoflex.autenticacion.exception.UsuarioNoEncontradoException;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import cl.pagoflex.autenticacion.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final CredencialRepository credencialRepository;
    private final UsuarioCliente usuarioCliente;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(CredencialRepository credencialRepository,
                           UsuarioCliente usuarioCliente,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.credencialRepository = credencialRepository;
        this.usuarioCliente = usuarioCliente;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public Credencial registrar(RegistroRequestDTO request) {
        if (credencialRepository.existsByCorreo(request.getCorreo())
                || credencialRepository.existsByUsuarioId(request.getUsuarioId())) {
            throw new CredencialDuplicadaException("Ya existe una credencial para el correo o usuario");
        }

        UsuarioCliente.UsuarioExterno usuario = usuarioCliente.obtenerPorId(request.getUsuarioId());
        if (usuario == null || usuario.id() == null) {
            throw new UsuarioNoEncontradoException();
        }
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new UsuarioInactivoException();
        }
        if (!usuario.correo().equalsIgnoreCase(request.getCorreo())) {
            throw new CredencialesInvalidasException();
        }

        Credencial credencial = new Credencial();
        credencial.setUsuarioId(usuario.id());
        credencial.setCorreo(usuario.correo());
        credencial.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        credencial.setActivo(true);
        return credencialRepository.save(credencial);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        Credencial credencial = credencialRepository.findByCorreo(request.getCorreo())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!credencial.isActivo()) {
            throw new UsuarioInactivoException();
        }
        if (!passwordEncoder.matches(request.getPassword(), credencial.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        UsuarioCliente.UsuarioExterno usuario = usuarioCliente.obtenerPorId(credencial.getUsuarioId());
        if (usuario == null) {
            throw new UsuarioNoEncontradoException();
        }
        if (!Boolean.TRUE.equals(usuario.activo())) {
            throw new UsuarioInactivoException();
        }
        if (usuario.rol() == null) {
            throw new UsuarioNoEncontradoException();
        }
        if ("EMPRESA".equals(usuario.rol())
                && (usuario.empresaId() == null || usuario.empresaId() <= 0)) {
            throw new ServicioUsuariosNoDisponibleException(
                    "ms-usuarios devolvió un empresaId inválido para un usuario EMPRESA");
        }

        String token = jwtService.generarToken(usuario.id(), usuario.correo(), usuario.rol(), usuario.empresaId());
        return new AuthResponseDTO(token, "Bearer", jwtService.getExpirationMs(),
                usuario.id(), usuario.empresaId(), usuario.correo(), usuario.rol());
    }

    @Override
    public UsuarioAutenticadoDTO validarJWT(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new cl.pagoflex.autenticacion.exception.TokenInvalidoException();
        }
        Jwt jwt = jwtService.decodificar(authorizationHeader.substring(7));
        return me(jwt);
    }

    @Override
    public UsuarioAutenticadoDTO me(Jwt jwt) {
        Object usuarioId = jwt.getClaim("usuarioId");
        if (usuarioId == null) {
            throw new cl.pagoflex.autenticacion.exception.TokenInvalidoException();
        }
        Long id = usuarioId instanceof Number number ? number.longValue() : Long.valueOf(usuarioId.toString());
        Object empresaId = jwt.getClaim("empresaId");
        Long idEmpresa = empresaId instanceof Number number ? number.longValue()
                : empresaId == null ? null : Long.valueOf(empresaId.toString());
        return new UsuarioAutenticadoDTO(id, idEmpresa, jwt.getSubject(), jwt.getClaimAsString("rol"));
    }

    @Override
    public void desactivar(Long usuarioId) {
        Credencial credencial = credencialRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new CredencialNotFoundException("No existe una credencial para el usuario"));
        credencial.setActivo(false);
        credencialRepository.save(credencial);
    }
}
