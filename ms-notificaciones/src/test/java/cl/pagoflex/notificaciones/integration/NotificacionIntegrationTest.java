package cl.pagoflex.notificaciones.integration;

import cl.pagoflex.notificaciones.dto.RespuestaNotificacionDTO;
import cl.pagoflex.notificaciones.dto.SolicitudCambioContrasenaDTO;
import cl.pagoflex.notificaciones.dto.SolicitudRecuperacionDTO;
import cl.pagoflex.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "services.internal-token=PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789",
        "eureka.client.enabled=false",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password",
        "mail.from=test@example.com"
})
@AutoConfigureMockMvc
class NotificacionIntegrationTest {
    private static final String TOKEN = "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionService notificacionService;

    @Test
    void rechazaSolicitudSinToken() throws Exception {
        mockMvc.perform(post("/api/notificaciones/recuperacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\",\"codigo\":\"123456\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaTokenIncorrecto() throws Exception {
        mockMvc.perform(post("/api/notificaciones/recuperacion")
                        .header("X-Internal-Token", "TOKEN_INCORRECTO_123456789012345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\",\"codigo\":\"123456\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void aceptaSolicitudInternaValida() throws Exception {
        when(notificacionService.enviarCodigoRecuperacion(any(SolicitudRecuperacionDTO.class)))
                .thenReturn(new RespuestaNotificacionDTO(true, "Correo enviado"));

        mockMvc.perform(post("/api/notificaciones/recuperacion")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\",\"codigo\":\"123456\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void aceptaConfirmacionDeCambioValida() throws Exception {
        when(notificacionService.enviarConfirmacionCambioContrasena(
                any(SolicitudCambioContrasenaDTO.class)))
                .thenReturn(new RespuestaNotificacionDTO(true, "Correo enviado"));

        mockMvc.perform(post("/api/notificaciones/cambio-contrasena")
                        .header("X-Internal-Token", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"usuario@example.com\"}"))
                .andExpect(status().isOk());
    }
}
