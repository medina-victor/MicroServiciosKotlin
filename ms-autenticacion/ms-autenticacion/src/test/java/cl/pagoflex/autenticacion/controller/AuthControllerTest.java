package cl.pagoflex.autenticacion.controller;

import cl.pagoflex.autenticacion.dto.AuthResponseDTO;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.service.AuthService;
import cl.pagoflex.autenticacion.service.RecuperacionContrasenaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RecuperacionContrasenaService recuperacionContrasenaService;

    @Test
    void registroValidaDatos() throws Exception {
        Credencial credencial = new Credencial();
        credencial.setUsuarioId(1L);
        credencial.setCorreo("cliente@pagoflex.cl");
        when(authService.registrar(any())).thenReturn(credencial);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId":1,"correo":"cliente@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.correo").value("cliente@pagoflex.cl"));
    }

    @Test
    void rechazaPasswordCorta() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId":1,"correo":"cliente@pagoflex.cl","password":"123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginPublicoDevuelveToken() throws Exception {
        when(authService.login(any())).thenReturn(
                new AuthResponseDTO("jwt", "Bearer", 3600000L, 1L, "cliente@pagoflex.cl", "CLIENTE"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"cliente@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void recuperacionEsPublica() throws Exception {
        mockMvc.perform(post("/api/auth/recuperacion/solicitar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@pagoflex.cl\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarContrasenaValidaDatos() throws Exception {
        mockMvc.perform(post("/api/auth/recuperacion/cambiar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"cliente@pagoflex.cl\",\"codigo\":\"123456\",\"nuevaContrasena\":\"Nueva123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void desactivarLlamaAlServicio() throws Exception {
        mockMvc.perform(patch("/api/auth/desactivar/1"))
                .andExpect(status().isNoContent());
    }
}
