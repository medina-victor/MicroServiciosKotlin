package cl.pagoflex.pagos.controller;

import cl.pagoflex.pagos.dto.PagoRequestDTO;
import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.service.PagoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "Registro y administración de pagos simulados")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    @Operation(summary = "Registrar un pago simulado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado como PENDIENTE"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "El compromiso ya tiene un pago EXITOSO")
    })
    public ResponseEntity<PagoResponseDTO> registrar(@Valid @RequestBody PagoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todos los pagos")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<PagoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar un pago por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PagoResponseDTO> buscarPorId(
            @Parameter(description = "Identificador del pago", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(pagoService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Buscar pagos por usuario")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pagoService.buscarPorUsuario(usuarioId));
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Buscar pagos por empresa")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(pagoService.buscarPorEmpresa(empresaId));
    }

    @GetMapping("/compromiso/{compromisoId}")
    @Operation(summary = "Buscar pagos por compromiso")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorCompromiso(@PathVariable Long compromisoId) {
        return ResponseEntity.ok(pagoService.buscarPorCompromiso(compromisoId));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Buscar pagos por estado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido"),
            @ApiResponse(responseCode = "400", description = "Estado inválido")
    })
    public ResponseEntity<List<PagoResponseDTO>> buscarPorEstado(@PathVariable EstadoPago estado) {
        return ResponseEntity.ok(pagoService.buscarPorEstado(estado));
    }

    @PatchMapping("/{id}/exitoso")
    @Operation(summary = "Marcar un pago PENDIENTE como EXITOSO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida")
    })
    public ResponseEntity<PagoResponseDTO> marcarExitoso(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.marcarExitoso(id));
    }

    @PatchMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar un pago PENDIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago rechazado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida")
    })
    public ResponseEntity<PagoResponseDTO> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.rechazar(id));
    }

    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular un pago PENDIENTE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago anulado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "409", description = "Transición no permitida")
    })
    public ResponseEntity<PagoResponseDTO> anular(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.anular(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pago que no esté EXITOSO")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se permite eliminar un pago EXITOSO")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
