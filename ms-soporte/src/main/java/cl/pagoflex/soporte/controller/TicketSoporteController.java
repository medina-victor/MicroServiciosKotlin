package cl.pagoflex.soporte.controller;

import cl.pagoflex.soporte.dto.TicketEstadoDTO;
import cl.pagoflex.soporte.dto.TicketRespuestaDTO;
import cl.pagoflex.soporte.dto.TicketSoporteRequestDTO;
import cl.pagoflex.soporte.dto.TicketSoporteResponseDTO;
import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.security.AccesoPropietario;
import cl.pagoflex.soporte.service.TicketSoporteService;
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

import java.util.List;

@RestController
@RequestMapping("/api/soporte")
@Tag(name = "Soporte", description = "Administración de solicitudes de soporte de PagoFlex")
public class TicketSoporteController {

    private final TicketSoporteService service;

    public TicketSoporteController(TicketSoporteService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE') or (hasRole('CLIENTE') and #request.usuarioId.toString() == authentication.tokenAttributes['usuarioId'].toString()) or (hasRole('EMPRESA') and #request.empresaId != null and #request.empresaId.toString() == authentication.tokenAttributes['empresaId'].toString())")
    @Operation(summary = "Crear solicitud de soporte")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<TicketSoporteResponseDTO> crear(
            @Valid @RequestBody TicketSoporteRequestDTO request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crear(request, AccesoPropietario.desde(authentication)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    @Operation(summary = "Listar todas las solicitudes")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<TicketSoporteResponseDTO>> listarTodas(Authentication authentication) {
        return ResponseEntity.ok(service.listarTodas(AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ticket por ID")
    public ResponseEntity<TicketSoporteResponseDTO> buscarPorId(
            @Parameter(description = "Identificador del ticket", example = "1") @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorId(id, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar tickets por usuario")
    public ResponseEntity<List<TicketSoporteResponseDTO>> buscarPorUsuario(
            @PathVariable Long usuarioId, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorUsuario(usuarioId, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Buscar tickets por empresa")
    public ResponseEntity<List<TicketSoporteResponseDTO>> buscarPorEmpresa(
            @PathVariable Long empresaId, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorEmpresa(empresaId, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar tickets por estado")
    public ResponseEntity<List<TicketSoporteResponseDTO>> buscarPorEstado(
            @PathVariable EstadoTicket estado, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorEstado(estado, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar tickets por categoría")
    public ResponseEntity<List<TicketSoporteResponseDTO>> buscarPorCategoria(
            @PathVariable CategoriaSoporte categoria, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorCategoria(categoria, AccesoPropietario.desde(authentication)));
    }

    @GetMapping("/prioridad/{prioridad}")
    @Operation(summary = "Buscar tickets por prioridad")
    public ResponseEntity<List<TicketSoporteResponseDTO>> buscarPorPrioridad(
            @PathVariable PrioridadTicket prioridad, Authentication authentication) {
        return ResponseEntity.ok(service.buscarPorPrioridad(prioridad, AccesoPropietario.desde(authentication)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar información general del ticket")
    public ResponseEntity<TicketSoporteResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody TicketSoporteRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.ok(service.actualizar(id, request, AccesoPropietario.desde(authentication)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el estado del ticket")
    public ResponseEntity<TicketSoporteResponseDTO> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody TicketEstadoDTO dto) {
        return ResponseEntity.ok(service.cambiarEstado(id, dto));
    }

    @PatchMapping("/{id}/responder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Responder el ticket")
    public ResponseEntity<TicketSoporteResponseDTO> responder(
            @PathVariable Long id, @Valid @RequestBody TicketRespuestaDTO dto) {
        return ResponseEntity.ok(service.responder(id, dto));
    }

    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cerrar el ticket")
    public ResponseEntity<TicketSoporteResponseDTO> cerrar(@PathVariable Long id) {
        return ResponseEntity.ok(service.cerrar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ticket no cerrado")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
