package cl.pagoflex.compromisos.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:compromisos_integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class CompromisoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void flujoBasicoCrud() throws Exception {
        String json = """
                {"usuarioId":1,"empresaId":2,"descripcion":"Cuota","monto":1000,
                 "fechaVencimiento":"2099-12-31","observaciones":"Inicial"}
                """;

        String response = mockMvc.perform(post("/api/compromisos")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andReturn().getResponse().getContentAsString();
        long id = org.springframework.boot.json.JsonParserFactory
                .getJsonParser().parseMap(response).get("id") instanceof Number number
                ? number.longValue() : 0L;

        mockMvc.perform(get("/api/compromisos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Cuota"));

        mockMvc.perform(put("/api/compromisos/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.replace("Cuota", "Cuota actualizada")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Cuota actualizada"));

        mockMvc.perform(patch("/api/compromisos/" + id + "/pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADO"));

        mockMvc.perform(delete("/api/compromisos/" + id))
                .andExpect(status().isNoContent());
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
