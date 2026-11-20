package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.dto.AuthResponseDTO;
import cl.pagoflex.autenticacion.dto.LoginRequestDTO;
import cl.pagoflex.autenticacion.dto.RegistroRequestDTO;
import cl.pagoflex.autenticacion.dto.UsuarioAutenticadoDTO;
import cl.pagoflex.autenticacion.model.Credencial;

public interface AuthService {
    Credencial registrar(RegistroRequestDTO request);
    AuthResponseDTO login(LoginRequestDTO request);
    UsuarioAutenticadoDTO validarJWT(String authorizationHeader);
    UsuarioAutenticadoDTO me(org.springframework.security.oauth2.jwt.Jwt jwt);
    void desactivar(Long usuarioId);
}
