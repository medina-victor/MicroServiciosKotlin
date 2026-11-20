package cl.pagoflex.compromisos.controller;

import cl.pagoflex.compromisos.dto.CompromisoRequestDTO;
import cl.pagoflex.compromisos.dto.CompromisoResponseDTO;
import cl.pagoflex.compromisos.exception.RecursoNoEncontradoException;
import cl.pagoflex.compromisos.model.EstadoCompromiso;
import cl.pagoflex.compromisos.service.CompromisoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompromisoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class CompromisoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompromisoService service;

    private CompromisoResponseDTO response;

    @BeforeEach
    void setUp() {
        response = new CompromisoResponseDTO(1L, 10L, 20L, "Cuota",
                new BigDecimal("1000"), java.time.LocalDateTime.now(),
                LocalDate.now().plusDays(10), EstadoCompromiso.PENDIENTE, null);
    }

    @Test
    void postGetPutPatchYDelete() throws Exception {
        String json = """
                {"usuarioId":10,"empresaId":20,"descripcion":"Cuota","monto":1000,
                 "fechaVencimiento":"2099-12-31"}
                """;
        when(service.registrar(any(CompromisoRequestDTO.class))).thenReturn(response);
        when(service.buscarPorId(1L)).thenReturn(response);
        when(service.actualizar(eq(1L), any(CompromisoRequestDTO.class))).thenReturn(response);
        when(service.pagar(1L)).thenReturn(response);
        when(service.cancelar(1L)).thenReturn(response);

        mockMvc.perform(post("/api/compromisos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/compromisos/1")).andExpect(status().isOk());
        mockMvc.perform(put("/api/compromisos/1").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/compromisos/1/pagar")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/compromisos/1/cancelar")).andExpect(status().isOk());
        mockMvc.perform(delete("/api/compromisos/1")).andExpect(status().isNoContent());
    }

    @Test
    void datosInvalidosYNoEncontrado() throws Exception {
        mockMvc.perform(post("/api/compromisos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":-1,\"empresaId\":20,\"descripcion\":\"\",\"monto\":0,\"fechaVencimiento\":\"2020-01-01\"}"))
                .andExpect(status().isBadRequest());

        when(service.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException(99L));
        mockMvc.perform(get("/api/compromisos/99")).andExpect(status().isNotFound());
    }
}
