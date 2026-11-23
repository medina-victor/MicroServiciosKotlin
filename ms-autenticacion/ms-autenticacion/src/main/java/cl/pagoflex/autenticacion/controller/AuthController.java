package cl.pagoflex.autenticacion.controller;

import cl.pagoflex.autenticacion.dto.AuthResponseDTO;
import cl.pagoflex.autenticacion.dto.LoginRequestDTO;
import cl.pagoflex.autenticacion.dto.RegistroRequestDTO;
import cl.pagoflex.autenticacion.dto.UsuarioAutenticadoDTO;
import cl.pagoflex.autenticacion.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.CambioContrasenaRecuperacionDTO;
import cl.pagoflex.autenticacion.dto.RespuestaRecuperacionDTO;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.service.AuthService;
import cl.pagoflex.autenticacion.service.RecuperacionContrasenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y validación de tokens")
public class AuthController {

    private final AuthService authService;
    private final RecuperacionContrasenaService recuperacionContrasenaService;

    public AuthController(AuthService authService,
                          RecuperacionContrasenaService recuperacionContrasenaService) {
        this.authService = authService;
        this.recuperacionContrasenaService = recuperacionContrasenaService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar credenciales para un usuario existente")
    public ResponseEntity<UsuarioAutenticadoDTO> registrar(@Valid @RequestBody RegistroRequestDTO request) {
        Credencial credencial = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UsuarioAutenticadoDTO(credencial.getUsuarioId(), credencial.getCorreo(), null));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperacion/solicitar")
    @Operation(summary = "Solicitar recuperacion de contraseña")
    public ResponseEntity<RespuestaRecuperacionDTO> solicitarRecuperacion(
            @Valid @RequestBody SolicitudRecuperacionDTO request) {
        return ResponseEntity.ok(recuperacionContrasenaService.solicitar(request));
    }

    @PostMapping("/recuperacion/cambiar")
    @Operation(summary = "Cambiar contraseña usando el codigo de recuperacion")
    public ResponseEntity<RespuestaRecuperacionDTO> cambiarContrasena(
            @Valid @RequestBody CambioContrasenaRecuperacionDTO request) {
        return ResponseEntity.ok(recuperacionContrasenaService.cambiar(request));
    }

    @GetMapping("/validar")
    @Operation(summary = "Validar el JWT recibido", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsuarioAutenticadoDTO> validar(
            @RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(authService.validarJWT(authorizationHeader));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsuarioAutenticadoDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.me(jwt));
    }

    @PatchMapping("/desactivar/{usuarioId}")
    @Operation(summary = "Desactivar credenciales", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> desactivar(@PathVariable Long usuarioId) {
        authService.desactivar(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
