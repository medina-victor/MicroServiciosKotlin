package cl.pagoflex.soporte.integration;

import cl.pagoflex.soporte.client.EmpresaClient;
import cl.pagoflex.soporte.client.UsuarioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-only-secret-with-at-least-32-bytes",
        "services.internal-token=test-internal-token",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:soporte_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class TicketSoporteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UsuarioClient usuarioClient;
    @MockBean
    private EmpresaClient empresaClient;

    @BeforeEach
    void prepararDependencias() {
        org.mockito.Mockito.when(usuarioClient.buscarPorId(1L))
                .thenReturn(new UsuarioClient.UsuarioExterno(1L, true));
        org.mockito.Mockito.when(empresaClient.buscarPorId(2L))
                .thenReturn(new EmpresaClient.EmpresaExterna(2L, true));
    }

    @Test
    void flujoCreacionConsultaRespuestaYCierre() throws Exception {
        String json = """
                {"usuarioId":1,"empresaId":2,"asunto":"Problema con un pago",
                 "descripcion":"Realicé un pago pero aparece pendiente","categoria":"PAGOS","prioridad":"MEDIA"}
                """;
        String response = mockMvc.perform(post("/api/soporte")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andReturn().getResponse().getContentAsString();
        long id = org.springframework.boot.json.JsonParserFactory
                .getJsonParser().parseMap(response).get("id") instanceof Number number
                ? number.longValue() : 0L;

        mockMvc.perform(get("/api/soporte/" + id).header("X-Internal-Token", "test-internal-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Realicé un pago pero aparece pendiente"));
        mockMvc.perform(patch("/api/soporte/" + id + "/responder")
                        .header("X-Internal-Token", "test-internal-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\"Estamos revisando la información de tu pago\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESPONDIDO"));
        mockMvc.perform(patch("/api/soporte/" + id + "/cerrar")
                        .header("X-Internal-Token", "test-internal-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"))
                .andExpect(jsonPath("$.fechaCierre").exists());
    }

    @Test
    void publicaSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ms-soporte - PagoFlex"));
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
