package service.eventos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

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

    private UUID organizerId;
    private UUID clienteId;

    // cria um DTO válido
    private EventoRequisicaoDto criarRequisicaoValida() {
        EventoRequisicaoDto requisicao = new EventoRequisicaoDto();
        requisicao.setNome("Show de Lançamento");
        requisicao.setDescricao("Nova banda de rock");
        requisicao.setLocalizacao("Clube Central");
        requisicao.setData(LocalDateTime.now().plusMonths(1));
        requisicao.setCapacidade(200);
        requisicao.setCategoriaId(1L);
        return requisicao;
    }

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
    }

    //testes de Endpoints Públicos

    @Test
    void deveListarEventosDisponiveis() throws Exception {
        Page<EventoRespostaDto> paginaDeEventos = new PageImpl<>(List.of(new EventoRespostaDto()));
        when(eventoService.listarEventosDisponiveis(any())).thenReturn(paginaDeEventos);

        mockMvc.perform(get("/eventos")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("testuser")))
                .andExpect(status().isOk());
    }

    @Test
    void deveBuscarEventoPorId() throws Exception {
        EventoRespostaDto evento = new EventoRespostaDto();
        evento.setId(1L);
        when(eventoService.buscarPorId(1L)).thenReturn(evento);

        mockMvc.perform(get("/eventos/{id}", 1L)
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }


    //Testes de organizador

    @Test
    void deveCriarEventoERetornarStatusCreated() throws Exception {
        EventoRequisicaoDto requisicao = criarRequisicaoValida();
        EventoRespostaDto resposta = new EventoRespostaDto();
        resposta.setId(1L);
        resposta.setNome("Show de Lançamento");
        resposta.setOrganizerId(organizerId);

        when(eventoService.criarEvento(any(EventoRequisicaoDto.class), eq(organizerId))).thenReturn(resposta);

        mockMvc.perform(post("/eventos/criar-evento")
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "ORGANIZADOR")
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
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "ORGANIZADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicaoInvalida))
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void naoDeveCriarEventoSeRoleNaoForOrganizador() throws Exception {
        EventoRequisicaoDto requisicao = criarRequisicaoValida();

        mockMvc.perform(post("/eventos/criar-evento")
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "CLIENTE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao))
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBuscarEventosDoOrganizadorERetornarPagina() throws Exception {
        EventoRespostaDto eventoDto = new EventoRespostaDto();
        eventoDto.setId(10L);
        Page<EventoRespostaDto> paginaDeEventos = new PageImpl<>(List.of(eventoDto));

        when(eventoService.buscarEventosDoOrganizador(eq(organizerId), any())).thenReturn(paginaDeEventos);

        mockMvc.perform(get("/eventos/meus-eventos")
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "ORGANIZADOR")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void deveDeletarEventoERetornarNoContent() throws Exception {
        Long eventoId = 1L;
        doNothing().when(eventoService).deletarEvento(eventoId, organizerId);

        mockMvc.perform(delete("/eventos/{eventoId}", eventoId)
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "ORGANIZADOR")
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isNoContent());
    }

    //Testes de Cliente

    @Test
    void deveInscreverEmEventoComSucesso() throws Exception {
        Long eventoId = 1L;
        doNothing().when(eventoService).inscreverEmEvento(eventoId, clienteId);

        mockMvc.perform(post("/eventos/{eventoId}/inscrever", eventoId)
                        .header("X-User-Id", clienteId.toString())
                        .header("X-User-Roles", "CLIENTE")
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void naoDeveInscreverSeNaoForCliente() throws Exception {
        Long eventoId = 1L;

        mockMvc.perform(post("/eventos/{eventoId}/inscrever", eventoId)
                        .header("X-User-Id", organizerId.toString())
                        .header("X-User-Roles", "ORGANIZADOR")
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Apenas CLIENTE pode se inscrever."));
    }

    @Test
    void naoDeveInscreverSeNaoAutenticado() throws Exception {
        Long eventoId = 1L;

        mockMvc.perform(post("/eventos/{eventoId}/inscrever", eventoId)
                        .with(user("testuser")).with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Usuário não autenticado."));
    }

    @Test
    void deveBuscarMinhasInscricoesComSucesso() throws Exception {
        Page<EventoRespostaDto> paginaDeEventos = new PageImpl<>(List.of(new EventoRespostaDto()));
        when(eventoService.buscarInscricoesDoParticipante(eq(clienteId), any())).thenReturn(paginaDeEventos);

        mockMvc.perform(get("/eventos/minhas-inscricoes")
                        .header("X-User-Id", clienteId.toString())
                        .header("X-User-Roles", "CLIENTE")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("testuser")))
                .andExpect(status().isOk());
    }
}