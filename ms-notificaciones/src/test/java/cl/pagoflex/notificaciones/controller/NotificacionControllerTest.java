package cl.pagoflex.notificaciones.controller;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.notificaciones.exception.GlobalExceptionHandler;
import cl.pagoflex.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {
    @Mock
    private NotificacionService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificacionController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rechazaCorreoInvalido() throws Exception {
        mockMvc.perform(post("/api/notificaciones/recuperacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"no-es-correo\",\"codigo\":\"123456\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaCodigoVacio() throws Exception {
        mockMvc.perform(post("/api/notificaciones/recuperacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\",\"codigo\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviaConfirmacion() throws Exception {
        when(service.enviarConfirmacionCambioContrasena(any(SolicitudCambioContrasenaDTO.class)))
                .thenReturn(new RespuestaNotificacionDTO(true, "Correo enviado"));

        mockMvc.perform(post("/api/notificaciones/cambio-contrasena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enviado").value(true));
    }
}
