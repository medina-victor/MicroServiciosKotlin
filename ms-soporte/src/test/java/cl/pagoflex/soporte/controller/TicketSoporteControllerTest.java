package cl.pagoflex.soporte.controller;

import cl.pagoflex.soporte.dto.TicketRespuestaDTO;
import cl.pagoflex.soporte.dto.TicketSoporteResponseDTO;
import cl.pagoflex.soporte.exception.TicketNoEncontradoException;
import cl.pagoflex.soporte.model.CategoriaSoporte;
import cl.pagoflex.soporte.model.EstadoTicket;
import cl.pagoflex.soporte.model.PrioridadTicket;
import cl.pagoflex.soporte.security.AccesoPropietario;
import cl.pagoflex.soporte.service.TicketSoporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TicketSoporteControllerTest {

    @Mock
    private TicketSoporteService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TicketSoporteController(service))
                .defaultRequest(get("/").principal(new UsernamePasswordAuthenticationToken(
                        "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))))
                .setControllerAdvice(new cl.pagoflex.soporte.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void crearListarYBuscar() throws Exception {
        TicketSoporteResponseDTO dto = respuesta();
        when(service.crear(any(), any(AccesoPropietario.class))).thenReturn(dto);
        when(service.listarTodas(any(AccesoPropietario.class))).thenReturn(List.of(dto));
        when(service.buscarPorId(eq(1L), any(AccesoPropietario.class))).thenReturn(dto);
        when(service.buscarPorUsuario(eq(1L), any(AccesoPropietario.class))).thenReturn(List.of(dto));
        when(service.buscarPorEmpresa(eq(2L), any(AccesoPropietario.class))).thenReturn(List.of(dto));

        mockMvc.perform(post("/api/soporte").contentType(MediaType.APPLICATION_JSON).content(creacionJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("ABIERTO"));
        mockMvc.perform(get("/api/soporte")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
        mockMvc.perform(get("/api/soporte/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/soporte/usuario/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/soporte/empresa/2")).andExpect(status().isOk());
    }

    @Test
    void responderYCerrar() throws Exception {
        TicketSoporteResponseDTO respondido = respuesta();
        respondido.setEstado(EstadoTicket.RESPONDIDO);
        TicketSoporteResponseDTO cerrado = respuesta();
        cerrado.setEstado(EstadoTicket.CERRADO);
        when(service.responder(eq(1L), any(TicketRespuestaDTO.class))).thenReturn(respondido);
        when(service.cerrar(1L)).thenReturn(cerrado);

        mockMvc.perform(patch("/api/soporte/1/responder").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\"Estamos revisando\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("RESPONDIDO"));
        mockMvc.perform(patch("/api/soporte/1/cerrar"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CERRADO"));
    }

    @Test
    void devuelveError404Y400() throws Exception {
        when(service.buscarPorId(eq(99L), any(AccesoPropietario.class)))
                .thenThrow(new TicketNoEncontradoException(99L));

        mockMvc.perform(get("/api/soporte/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(post("/api/soporte").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":0,\"asunto\":\"\",\"descripcion\":\"\",\"categoria\":\"PAGOS\"}"))
                .andExpect(status().isBadRequest());
    }

    private TicketSoporteResponseDTO respuesta() {
        TicketSoporteResponseDTO dto = new TicketSoporteResponseDTO();
        dto.setId(1L);
        dto.setUsuarioId(1L);
        dto.setEmpresaId(2L);
        dto.setAsunto("Problema con un pago");
        dto.setDescripcion("El pago aparece pendiente");
        dto.setCategoria(CategoriaSoporte.PAGOS);
        dto.setPrioridad(PrioridadTicket.MEDIA);
        dto.setEstado(EstadoTicket.ABIERTO);
        return dto;
    }

    private String creacionJson() {
        return "{\"usuarioId\":1,\"empresaId\":2,\"asunto\":\"Problema con un pago\","
                + "\"descripcion\":\"El pago aparece pendiente\",\"categoria\":\"PAGOS\",\"prioridad\":\"MEDIA\"}";
    }
}
