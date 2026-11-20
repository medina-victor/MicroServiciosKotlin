package cl.pagoflex.usuarios.controller;

import cl.pagoflex.usuarios.dto.UsuarioRequest;
import cl.pagoflex.usuarios.dto.UsuarioResponse;
import cl.pagoflex.usuarios.dto.UsuarioRolRequest;
import cl.pagoflex.usuarios.model.RolUsuario;
import cl.pagoflex.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Administración independiente de usuarios PagoFlex")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @Operation(summary = "Registrar usuario", description = "Crea un usuario con rol CLIENTE. No acepta rol ni datos administrativos.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado")
    })
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(request));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Lista todos los usuarios registrados.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID", description = "Obtiene el usuario cuyo identificador coincide.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @Parameter(description = "Identificador del usuario", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/correo/{correo}")
    @Operation(summary = "Buscar por correo", description = "Busca exactamente por la dirección de correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> buscarPorCorreo(
            @Parameter(description = "Correo electrónico", example = "juan@demo.cl") @PathVariable String correo) {
        return ResponseEntity.ok(usuarioService.buscarPorCorreo(correo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza nombre, correo y teléfono; no modifica rol, estado ni fecha de registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "409", description = "Correo ya registrado por otro usuario")
    })
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @Parameter(description = "Identificador del usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina físicamente el registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "Identificador del usuario") @PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar usuario", description = "Operación administrativa pendiente de protección con autenticación. Cambia activo a false sin eliminar el registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desactivado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> desactivarUsuario(
            @Parameter(description = "Identificador del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desactivarUsuario(id));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Cambiar rol", description = "Operación administrativa expuesta sin autenticación por ahora. Debe protegerse antes de publicar el servicio. EMPRESA requiere empresaId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o falta empresaId para EMPRESA"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @Parameter(description = "Identificador del usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioRolRequest request) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request));
    }

    @GetMapping("/rol/{rol}")
    @Operation(summary = "Listar por rol", description = "Consulta administrativa pendiente de protección con autenticación. Roles válidos: CLIENTE, EMPRESA, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Rol inválido")
    })
    public ResponseEntity<List<UsuarioResponse>> listarPorRol(
            @Parameter(description = "Rol a consultar", example = "CLIENTE") @PathVariable RolUsuario rol) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rol));
    }
}
