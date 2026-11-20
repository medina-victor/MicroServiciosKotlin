package cl.pagoflex.empresas.controller;

import cl.pagoflex.empresas.dto.EmpresaRequestDTO;
import cl.pagoflex.empresas.dto.EmpresaResponseDTO;
import cl.pagoflex.empresas.exception.RecursoDuplicadoException;
import cl.pagoflex.empresas.exception.RecursoNoEncontradoException;
import cl.pagoflex.empresas.service.EmpresaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmpresaService empresaService;

    @Test
    void postDevuelve201() throws Exception {
        when(empresaService.registrar(any(EmpresaRequestDTO.class))).thenReturn(response(1L));

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("76.123.456-7")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("76.123.456-7"));
    }

    @Test
    void getDevuelve200() throws Exception {
        when(empresaService.listarTodas()).thenReturn(List.of(response(1L)));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreComercial").value("Demo"));
    }

    @Test
    void getPorIdDevuelve200O404() throws Exception {
        when(empresaService.buscarPorId(1L)).thenReturn(response(1L));
        when(empresaService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException(99L));

        mockMvc.perform(get("/api/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mockMvc.perform(get("/api/empresas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ruta").value("/api/empresas/99"));
    }

    @Test
    void putDevuelve200Y409() throws Exception {
        when(empresaService.actualizar(eq(1L), any(EmpresaRequestDTO.class))).thenReturn(response(1L));
        when(empresaService.actualizar(eq(2L), any(EmpresaRequestDTO.class)))
                .thenThrow(new RecursoDuplicadoException("RUT duplicado"));

        mockMvc.perform(put("/api/empresas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("76.123.456-7")))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/empresas/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("76.123.456-7")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void deleteDevuelve204() throws Exception {
        doNothing().when(empresaService).eliminar(1L);

        mockMvc.perform(delete("/api/empresas/1"))
                .andExpect(status().isNoContent());
        verify(empresaService).eliminar(1L);
    }

    @Test
    void datosInvalidosDevuelven400() throws Exception {
        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"razonSocial":" ","nombreComercial":"","rut":"rut-invalido",
                                 "correo":"correo-invalido","telefono":"","direccion":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void rutDuplicadoDevuelve409() throws Exception {
        when(empresaService.registrar(any(EmpresaRequestDTO.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe una empresa con ese RUT"));

        mockMvc.perform(post("/api/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("76.123.456-7")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void puedeActivarYDesactivar() throws Exception {
        when(empresaService.desactivar(1L)).thenReturn(response(1L, false));
        when(empresaService.activar(1L)).thenReturn(response(1L, true));

        mockMvc.perform(patch("/api/empresas/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mockMvc.perform(patch("/api/empresas/1/activar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    private String json(String rut) {
        return """
                {"razonSocial":"Comercial Demo SpA","nombreComercial":"Demo","rut":"%s",
                 "correo":"contacto@demo.cl","telefono":"+56 9 1234 5678",
                 "direccion":"Av. Providencia 123","descripcion":"Empresa de prueba"}
                """.formatted(rut);
    }

    private EmpresaResponseDTO response(Long id) {
        return response(id, true);
    }

    private EmpresaResponseDTO response(Long id, boolean activo) {
        return new EmpresaResponseDTO(id, "Comercial Demo SpA", "Demo", "76.123.456-7",
                "contacto@demo.cl", "+56 9 1234 5678", "Av. Providencia 123",
                "Empresa de prueba", activo, LocalDateTime.now());
    }
}
