package cl.pagoflex.pagos.integration;

import cl.pagoflex.pagos.client.CompromisoClient;
import cl.pagoflex.pagos.client.EmpresaClient;
import cl.pagoflex.pagos.client.UsuarioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-only-secret-with-at-least-32-bytes",
        "services.internal-token=PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:pagos_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class PagoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CompromisoClient compromisoClient;
    @MockBean
    private UsuarioClient usuarioClient;
    @MockBean
    private EmpresaClient empresaClient;

    @BeforeEach
    void prepararDependencias() {
        org.mockito.Mockito.when(compromisoClient.buscarPorId(100L)).thenReturn(
                new CompromisoClient.CompromisoExterno(100L, 10L, 20L,
                        new BigDecimal("1250.00"), "PENDIENTE"));
        org.mockito.Mockito.when(usuarioClient.buscarPorId(10L))
                .thenReturn(new UsuarioClient.UsuarioExterno(10L, true));
        org.mockito.Mockito.when(empresaClient.buscarPorId(20L))
                .thenReturn(new EmpresaClient.EmpresaExterna(20L, true));
    }

    @Test
    void registraConsultaYGestionaEstadosYRestricciones() throws Exception {
        String json = """
                {"compromisoId":100,"usuarioId":10,"empresaId":20,"monto":1250.00,
                 "metodoPago":"TRANSFERENCIA","descripcion":"Cuota"}
                """;
        String response = mockMvc.perform(post("/api/pagos")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.fechaPago").exists())
                .andExpect(jsonPath("$.referenciaTransaccion").exists())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonParserFactory.getJsonParser().parseMap(response).get("id")).longValue();

        mockMvc.perform(get("/api/pagos/" + id)
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compromisoId").value(100));
        mockMvc.perform(patch("/api/pagos/" + id + "/exitoso")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXITOSO"));
        mockMvc.perform(post("/api/pagos")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mockMvc.perform(delete("/api/pagos/" + id)
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isConflict());
    }

    @Test
    void publicaSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ms-pagos - PagoFlex"))
                .andExpect(jsonPath("$.paths['/api/pagos']").exists());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeEjecutarOperacionAdministrativa() throws Exception {
        mockMvc.perform(patch("/api/pagos/1/exitoso"))
                .andExpect(status().isForbidden());
    }
}
