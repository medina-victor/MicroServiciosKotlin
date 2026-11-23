package cl.pagoflex.autenticacion.integration;

import cl.pagoflex.autenticacion.client.UsuarioCliente;
import cl.pagoflex.autenticacion.client.NotificacionesCliente;
import cl.pagoflex.autenticacion.dto.CambioContrasenaRecuperacionDTO;
import cl.pagoflex.autenticacion.exception.CodigoRecuperacionInvalidoException;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import cl.pagoflex.autenticacion.security.JwtService;
import cl.pagoflex.autenticacion.service.RecuperacionContrasenaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.oauth2.jwt.Jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=0123456789abcdef0123456789abcdef",
        "usuarios.service.url=http://localhost:65530"
})
@AutoConfigureMockMvc
class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CredencialRepository credencialRepository;

    @Autowired
    private RecuperacionContrasenaService recuperacionContrasenaService;

    @MockitoBean
    private UsuarioCliente usuarioCliente;

    @MockitoBean
    private NotificacionesCliente notificacionesCliente;

    @Test
    void flujoRegistroLoginYMe() throws Exception {
        UsuarioCliente.UsuarioExterno usuario =
                new UsuarioCliente.UsuarioExterno(1L, "cliente@pagoflex.cl", "CLIENTE", true);
        when(usuarioCliente.obtenerPorId(1L)).thenReturn(usuario);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId":1,"correo":"cliente@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.correo").value("cliente@pagoflex.cl"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"cliente@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        String token = jwtService.generarToken(1L, "cliente@pagoflex.cl", "CLIENTE");
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(1))
                .andExpect(jsonPath("$.empresaId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void incluyeEmpresaIdEnElJwtYEnLasRespuestasDeAutenticacion() throws Exception {
        UsuarioCliente.UsuarioExterno empresa =
                new UsuarioCliente.UsuarioExterno(2L, "empresa@pagoflex.cl", "EMPRESA", true, 20L);
        when(usuarioCliente.obtenerPorId(2L)).thenReturn(empresa);

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId":2,"correo":"empresa@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isCreated());

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"empresa@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(20))
                .andReturn().getResponse().getContentAsString();
        String token = org.springframework.boot.json.JsonParserFactory.getJsonParser()
                .parseMap(response).get("accessToken").toString();

        Jwt decoded = jwtService.decodificar(token);
        assertThat(((Number) decoded.getClaim("empresaId")).longValue()).isEqualTo(20L);
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresaId").value(20));
    }

    @Test
    void noEmiteTokenDeEmpresaSinEmpresaIdValido() throws Exception {
        when(usuarioCliente.obtenerPorId(3L)).thenReturn(
                new UsuarioCliente.UsuarioExterno(3L, "sin-empresa@pagoflex.cl", "EMPRESA", true));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuarioId":3,"correo":"sin-empresa@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"sin-empresa@pagoflex.cl","password":"Secreto123"}
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje").value(
                        "ms-usuarios devolvió un empresaId inválido para un usuario EMPRESA"));
    }

    @Test
    void persisteLosIntentosFallidosYBloqueaLaRecuperacion() throws Exception {
        Credencial credencial = new Credencial();
        credencial.setUsuarioId(999L);
        credencial.setCorreo("bloqueo@pagoflex.cl");
        credencial.setPasswordHash("hash-inicial");
        credencial.setActivo(true);
        credencial.setRecoveryCodeHash(hash("123456"));
        credencial.setRecoveryCodeExpiration(java.time.LocalDateTime.now().plusMinutes(10));
        credencial.setRecoveryCodeUsed(false);
        credencialRepository.save(credencial);

        CambioContrasenaRecuperacionDTO cambio = new CambioContrasenaRecuperacionDTO();
        cambio.setCorreo("bloqueo@pagoflex.cl");
        cambio.setCodigo("654321");
        cambio.setNuevaContrasena("Nueva123");

        for (int intento = 1; intento <= 5; intento++) {
            assertThatThrownBy(() -> recuperacionContrasenaService.cambiar(cambio))
                    .isInstanceOf(CodigoRecuperacionInvalidoException.class)
                    .hasMessageContaining("codigo de recuperacion");
            Credencial guardada = credencialRepository.findByCorreo("bloqueo@pagoflex.cl").orElseThrow();
            assertThat(guardada.getRecoveryAttempts()).isEqualTo(intento);
            if (intento == 5) {
                assertThat(guardada.getRecoveryLockedUntil()).isAfter(java.time.LocalDateTime.now());
            }
        }
    }

    private String hash(String valor) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(valor.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexadecimal = new StringBuilder();
        for (byte value : digest) {
            hexadecimal.append(String.format("%02x", value));
        }
        return hexadecimal.toString();
    }

    @Test
    void publicaSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ms-autenticacion - PagoFlex"));
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
