package cl.pagoflex.empresas.controller;

import cl.pagoflex.empresas.dto.EmpresaRequestDTO;
import cl.pagoflex.empresas.dto.EmpresaResponseDTO;
import cl.pagoflex.empresas.service.EmpresaService;
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
@RequestMapping("/api/empresas")
@Tag(name = "Empresas", description = "Administración independiente de empresas de PagoFlex")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    @Operation(summary = "Registrar empresa", description = "Valida y registra una empresa con RUT único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "RUT duplicado")
    })
    public ResponseEntity<EmpresaResponseDTO> registrar(@Valid @RequestBody EmpresaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.registrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar empresas", description = "Devuelve todas las empresas registradas.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido")
    public ResponseEntity<List<EmpresaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar empresa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido")
    })
    public ResponseEntity<EmpresaResponseDTO> buscarPorId(
            @Parameter(description = "Identificador de la empresa", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(empresaService.buscarPorId(id));
    }

    @GetMapping("/rut/{rut}")
    @Operation(summary = "Buscar empresa por RUT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaResponseDTO> buscarPorRut(
            @Parameter(description = "RUT registrado", example = "76.123.456-7") @PathVariable String rut) {
        return ResponseEntity.ok(empresaService.buscarPorRut(rut));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar empresa", description = "Actualiza datos de contacto y descripción. No modifica ID, estado ni fecha de registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada"),
            @ApiResponse(responseCode = "409", description = "RUT duplicado")
    })
    public ResponseEntity<EmpresaResponseDTO> actualizar(
            @Parameter(description = "Identificador de la empresa") @PathVariable Long id,
            @Valid @RequestBody EmpresaRequestDTO request) {
        return ResponseEntity.ok(empresaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar empresa", description = "Elimina físicamente el registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empresa eliminada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "Identificador de la empresa") @PathVariable Long id) {
        empresaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    @Operation(summary = "Desactivar empresa", description = "Cambia el estado activo a false sin eliminar el registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa desactivada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaResponseDTO> desactivar(
            @Parameter(description = "Identificador de la empresa") @PathVariable Long id) {
        return ResponseEntity.ok(empresaService.desactivar(id));
    }

    @PatchMapping("/{id}/activar")
    @Operation(summary = "Activar empresa", description = "Cambia el estado activo a true.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa activada"),
            @ApiResponse(responseCode = "404", description = "Empresa no encontrada")
    })
    public ResponseEntity<EmpresaResponseDTO> activar(
            @Parameter(description = "Identificador de la empresa") @PathVariable Long id) {
        return ResponseEntity.ok(empresaService.activar(id));
    }
}
