package cl.pagoflex.usuarios;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:usuarios_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class UsuarioControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiCompletaYDocumentacionOpenApi() throws Exception {
		String usuario = mockMvc.perform(post("/api/usuarios")
						.contentType("application/json")
						.content("""
								{"nombre":"Juan Perez","correo":"juan@demo.cl","telefono":"912345678"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.rol").value("CLIENTE"))
				.andExpect(jsonPath("$.activo").value(true))
				.andReturn().getResponse().getContentAsString();
		Long id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
				.readTree(usuario).get("id").asLong();

		mockMvc.perform(get("/api/usuarios"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].correo").value("juan@demo.cl"));
		mockMvc.perform(get("/api/usuarios/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));
		mockMvc.perform(get("/api/usuarios/correo/juan@demo.cl"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.correo").value("juan@demo.cl"));
		mockMvc.perform(put("/api/usuarios/{id}", id)
						.contentType("application/json")
						.content("""
								{"nombre":"Juan Actualizado","correo":"juan@demo.cl","telefono":"900000000"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nombre").value("Juan Actualizado"));

		mockMvc.perform(post("/api/usuarios")
						.contentType("application/json")
						.content("""
								{"nombre":"Duplicado","correo":"juan@demo.cl","telefono":"911111111"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("CONFLICT"));
		mockMvc.perform(post("/api/usuarios")
						.contentType("application/json")
						.content("""
								{"nombre":" ","correo":"correo-invalido"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.ruta").value("/api/usuarios"));

		mockMvc.perform(patch("/api/usuarios/{id}/rol", id)
						.contentType("application/json")
						.content("""
								{"rol":"EMPRESA","empresaId":15}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.empresaId").value(15));
		mockMvc.perform(get("/api/usuarios/rol/EMPRESA"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].rol").value("EMPRESA"));
		mockMvc.perform(patch("/api/usuarios/{id}/desactivar", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activo").value(false));
		mockMvc.perform(delete("/api/usuarios/{id}", id))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/usuarios/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("NOT_FOUND"));
		mockMvc.perform(get("/api/usuarios/rol/INVALIDO"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
						.string(containsString("/api/usuarios")));
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}
}
