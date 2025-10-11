package service.eventos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import service.eventos.controller.EventoController;
import service.eventos.dto.EventoRequisicaoDto;
import service.eventos.dto.EventoRespostaDto;
import service.eventos.service.EventoService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
//para simular usuario
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EventoService eventoService;

    @Test
    void deveCriarEventoERetornarStatusCreated() throws Exception {
        Long organizerId = 1L;
        EventoRequisicaoDto requisicao = new EventoRequisicaoDto();
        requisicao.setNome("Show de Lançamento");
        requisicao.setDescricao("Nova banda de rock");
        requisicao.setLocalizacao("Clube Central");
        requisicao.setData(LocalDateTime.now().plusMonths(1));
        requisicao.setCapacidade(200);
        requisicao.setCategoriaId(1L);

        EventoRespostaDto resposta = new EventoRespostaDto();
        resposta.setId(1L);
        resposta.setNome("Show de Lançamento");
        resposta.setOrganizerId(organizerId);

        when(eventoService.criarEvento(any(EventoRequisicaoDto.class), eq(organizerId))).thenReturn(resposta);

        mockMvc.perform(post("/eventos/criar-evento")
                        .header("X-User-ID", organizerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao))
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Show de Lançamento"));
    }

    @Test
    void deveRetornarBadRequestQuandoCriarEventoComDadosInvalidos() throws Exception {
        EventoRequisicaoDto requisicaoInvalida = new EventoRequisicaoDto();
        requisicaoInvalida.setNome("");

        mockMvc.perform(post("/eventos/criar-evento")
                        .header("X-User-ID", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoInvalida))
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarEventosDoOrganizadorERetornarPagina() throws Exception {
        Long organizerId = 5L;
        EventoRespostaDto eventoDto = new EventoRespostaDto();
        eventoDto.setId(10L);
        Page<EventoRespostaDto> paginaDeEventos = new PageImpl<>(List.of(eventoDto));

        when(eventoService.buscarEventosDoOrganizador(eq(organizerId), any())).thenReturn(paginaDeEventos);

        mockMvc.perform(get("/eventos/meus-eventos")
                        .header("X-User-ID", organizerId)
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("testuser"))) // Mantém o 'user'
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void deveDeletarEventoERetornarNoContent() throws Exception {
        Long eventoId = 1L;
        Long organizerId = 1L;
        doNothing().when(eventoService).deletarEvento(eventoId, organizerId);

        mockMvc.perform(delete("/eventos/meus-eventos/{eventoId}", eventoId)
                        .header("X-User-ID", organizerId)
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }
}