package cl.pagoflex.compromisos.controller;

import cl.pagoflex.compromisos.dto.CompromisoRequestDTO;
import cl.pagoflex.compromisos.dto.CompromisoResponseDTO;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import cl.pagoflex.compromisos.security.AccesoPropietario;
import cl.pagoflex.compromisos.service.CompromisoService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/compromisos")
@Tag(name = "Compromisos", description = "Administración independiente de compromisos de pago")
public class CompromisoController {

    private final CompromisoService compromisoService;

    public CompromisoController(CompromisoService compromisoService) {
        this.compromisoService = compromisoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE') or (hasRole('CLIENTE') and #request.usuarioId.toString() == authentication.tokenAttributes['usuarioId'].toString()) or (hasRole('EMPRESA') and #request.empresaId.toString() == authentication.tokenAttributes['empresaId'].toString())")
    @Operation(summary = "Registrar compromiso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compromiso registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<CompromisoResponseDTO> registrar(
            @Valid @RequestBody CompromisoRequestDTO request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                compromisoService.registrar(request, AccesoPropietario.desde(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Listar compromisos")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<CompromisoResponseDTO>> listarTodas(Authentication authentication) {
        return ResponseEntity.ok(compromisoService.listarTodas(AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar compromiso por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromiso encontrado"),
            @ApiResponse(responseCode = "404", description = "Compromiso no encontrado")
    })
    public ResponseEntity<CompromisoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador del compromiso", example = "1") @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(compromisoService.buscarPorId(id, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar compromisos por usuario")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<CompromisoResponseDTO>> buscarPorUsuario(
            @Parameter(description = "Identificador del usuario", example = "10") @PathVariable Long usuarioId,
            Authentication authentication) {
        return ResponseEntity.ok(compromisoService.buscarPorUsuario(usuarioId, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Buscar compromisos por empresa")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<CompromisoResponseDTO>> buscarPorEmpresa(
            @Parameter(description = "Identificador de la empresa", example = "20") @PathVariable Long empresaId,
            Authentication authentication) {
        return ResponseEntity.ok(compromisoService.buscarPorEmpresa(empresaId, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar compromisos por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<List<CompromisoResponseDTO>> buscarPorEstado(
            @Parameter(description = "Estado", example = "PENDIENTE") @PathVariable EstadoCompromiso estado,
            Authentication authentication) {
        return ResponseEntity.ok(compromisoService.buscarPorEstado(estado, AccesoPropietario.desde(authentication)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE') or (hasRole('CLIENTE') and #request.usuarioId.toString() == authentication.tokenAttributes['usuarioId'].toString()) or (hasRole('EMPRESA') and #request.empresaId.toString() == authentication.tokenAttributes['empresaId'].toString())")
    @Operation(summary = "Actualizar compromiso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromiso actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Compromiso no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se permite actualizar el estado actual")
    })
    public ResponseEntity<CompromisoResponseDTO> actualizar(
            @Parameter(description = "Identificador del compromiso") @PathVariable Long id,
            @Valid @RequestBody CompromisoRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(compromisoService.actualizar(id, request, AccesoPropietario.desde(authentication)));
    }

    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Marcar compromiso como pagado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromiso pagado"),
            @ApiResponse(responseCode = "404", description = "Compromiso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición de estado no permitida")
    })
    public ResponseEntity<CompromisoResponseDTO> pagar(
            @Parameter(description = "Identificador del compromiso") @PathVariable Long id,
            @RequestParam(required = false) Long pagoId) {
        return ResponseEntity.ok(compromisoService.pagar(id, pagoId));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancelar compromiso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compromiso cancelado"),
            @ApiResponse(responseCode = "404", description = "Compromiso no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición de estado no permitida")
    })
    public ResponseEntity<CompromisoResponseDTO> cancelar(
            @Parameter(description = "Identificador del compromiso") @PathVariable Long id) {
        return ResponseEntity.ok(compromisoService.cancelar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar compromiso")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Compromiso eliminado"),
            @ApiResponse(responseCode = "404", description = "Compromiso no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador del compromiso") @PathVariable Long id) {
        compromisoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
