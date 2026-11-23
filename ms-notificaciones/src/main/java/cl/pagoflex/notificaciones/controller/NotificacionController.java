package cl.pagoflex.notificaciones.controller;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "Envio interno de correos de PagoFlex")
@SecurityRequirement(name = "X-Internal-Token")
public class NotificacionController {
    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @PostMapping("/recuperacion")
    @Operation(summary = "Enviar codigo de recuperacion",
            description = "Recibe un codigo generado por ms-autenticacion y lo envia por correo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo enviado"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @ApiResponse(responseCode = "401", description = "Token interno ausente"),
            @ApiResponse(responseCode = "403", description = "Token interno invalido"),
            @ApiResponse(responseCode = "500", description = "Error interno"),
            @ApiResponse(responseCode = "503", description = "Servicio SMTP no disponible")
    })
    public ResponseEntity<RespuestaNotificacionDTO> enviarCodigoRecuperacion(
            @Valid @RequestBody SolicitudRecuperacionDTO solicitud) {
        return ResponseEntity.ok(notificacionService.enviarCodigoRecuperacion(solicitud));
    }

    @PostMapping("/cambio-contrasena")
    @Operation(summary = "Enviar confirmacion de cambio de contrasena")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Correo enviado"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida"),
            @ApiResponse(responseCode = "401", description = "Token interno ausente"),
            @ApiResponse(responseCode = "403", description = "Token interno invalido"),
            @ApiResponse(responseCode = "500", description = "Error interno"),
            @ApiResponse(responseCode = "503", description = "Servicio SMTP no disponible")
    })
    public ResponseEntity<RespuestaNotificacionDTO> enviarCambioContrasena(
            @Valid @RequestBody SolicitudCambioContrasenaDTO solicitud) {
        return ResponseEntity.ok(
                notificacionService.enviarConfirmacionCambioContrasena(solicitud));
    }
}
