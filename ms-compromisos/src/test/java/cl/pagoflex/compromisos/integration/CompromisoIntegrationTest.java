package cl.pagoflex.compromisos.integration;

import cl.pagoflex.compromisos.client.EmpresaClient;
import cl.pagoflex.compromisos.client.UsuarioClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-only-secret-with-at-least-32-bytes",
        "services.internal-token=PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:compromisos_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class CompromisoIntegrationTest {

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
    void flujoBasicoCrud() throws Exception {
        String json = """
                {"usuarioId":1,"empresaId":2,"descripcion":"Cuota","monto":1000,
                 "fechaVencimiento":"2099-12-31","observaciones":"Inicial"}
                """;

        String response = mockMvc.perform(post("/api/compromisos")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        long id = org.springframework.boot.json.JsonParserFactory
                .getJsonParser().parseMap(response).get("id") instanceof Number number
                ? number.longValue() : 0L;

        mockMvc.perform(get("/api/compromisos/" + id)
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Cuota"));

        mockMvc.perform(put("/api/compromisos/" + id)
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("Cuota", "Cuota actualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Cuota actualizada"));

        mockMvc.perform(patch("/api/compromisos/" + id + "/pagar")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));

        mockMvc.perform(delete("/api/compromisos/" + id)
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirmaElMismoPagoDeFormaIdempotente() throws Exception {
        String json = """
                {"usuarioId":1,"empresaId":2,"descripcion":"Cuota","monto":1000,
                 "fechaVencimiento":"2099-12-31"}
                """;
        String response = mockMvc.perform(post("/api/compromisos")
                        .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = org.springframework.boot.json.JsonParserFactory
                .getJsonParser().parseMap(response).get("id") instanceof Number number
                ? number.longValue() : 0L;

        for (int intento = 0; intento < 2; intento++) {
            mockMvc.perform(patch("/api/compromisos/" + id + "/pagar")
                            .queryParam("pagoId", "77")
                            .header("X-Internal-Token", "PAYOFLEX_TEST_INTERNAL_TOKEN_0123456789ABCDEF"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("PAGADO"));
        }
    }

    @Test
    void publicaSwagger() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API ms-compromisos - PagoFlex"))
                .andExpect(jsonPath("$.paths['/api/compromisos']").exists());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
