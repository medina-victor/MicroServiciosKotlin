package cl.pagoflex.pagos.controller;

import cl.pagoflex.pagos.dto.PagoResponseDTO;
import cl.pagoflex.pagos.exception.PagoNotFoundException;
import cl.pagoflex.pagos.model.EstadoPago;
import cl.pagoflex.pagos.model.MetodoPago;
import cl.pagoflex.pagos.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @Test
    void registraPagoValidado() throws Exception {
        when(pagoService.registrar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(response(1L, EstadoPago.PENDIENTE));
        String json = """
                {"compromisoId":100,"usuarioId":10,"empresaId":20,"monto":1250.00,
                 "metodoPago":"TRANSFERENCIA","descripcion":"Cuota"}
                """;

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.referenciaTransaccion").value("PAG-test"));
    }

    @Test
    void rechazaMontoNoPositivo() throws Exception {
        String json = """
                {"compromisoId":100,"usuarioId":10,"empresaId":20,"monto":0,
                 "metodoPago":"TRANSFERENCIA"}
                """;

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.path").value("/api/pagos"));
    }

    @Test
    void responde404UniformeParaPagoInexistente() throws Exception {
        when(pagoService.buscarPorId(99L)).thenThrow(new PagoNotFoundException(99L));

        mockMvc.perform(get("/api/pagos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/pagos/99"));
    }

    @Test
    void marcaPagoComoExitoso() throws Exception {
        when(pagoService.marcarExitoso(1L)).thenReturn(response(1L, EstadoPago.EXITOSO));

        mockMvc.perform(patch("/api/pagos/1/exitoso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXITOSO"));
    }

    private PagoResponseDTO response(Long id, EstadoPago estado) {
        return new PagoResponseDTO(id, 100L, 10L, 20L, new BigDecimal("1250.00"),
                LocalDateTime.now(), MetodoPago.TRANSFERENCIA, estado, "PAG-test", "Cuota");
    }
}
