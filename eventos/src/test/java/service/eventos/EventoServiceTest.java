package service.eventos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.eventos.client.IngressosClient;
import service.eventos.client.UserClient;
import service.eventos.commons.PaymentMethod;
import service.eventos.dto.EventoRequisicaoDto;
import service.eventos.exception.RecursoNaoEncontradoException;
import service.eventos.model.Categoria;
import service.eventos.model.Evento;
import service.eventos.model.StatusEvento;
import service.eventos.repository.CategoriaRepository;
import service.eventos.repository.EventoRepository;
import service.eventos.service.EventoService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private IngressosClient ingressosClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private EventoService eventoService;

    @Test
    void deveCriarEventoComSucesso() {
        UUID organizerId = UUID.randomUUID();
        EventoRequisicaoDto requisicao = criarEventoRequisicaoMock();
        Categoria categoria = new Categoria();
        categoria.setId(requisicao.getCategoriaId());

        when(categoriaRepository.findById(requisicao.getCategoriaId())).thenReturn(Optional.of(categoria));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento eventoSalvo = invocation.getArgument(0);
            eventoSalvo.setId(1L);
            return eventoSalvo;
        });

        UserClient.UserRespostaDto mockUser = new UserClient.UserRespostaDto();
        mockUser.setId(organizerId);
        mockUser.setNome("Organizador Teste");
        mockUser.setTipo("ORGANIZADOR");
        when(userClient.getUserById(organizerId)).thenReturn(mockUser);

        var resposta = eventoService.criarEvento(requisicao, organizerId);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getNome()).isEqualTo(requisicao.getNome());
        assertThat(resposta.getOrganizerId()).isEqualTo(organizerId);
        assertThat(resposta.getStatus()).isEqualTo(StatusEvento.ATIVO);
        verify(eventoRepository).save(any(Evento.class));
        verify(userClient).getUserById(organizerId);
    }

    @Test
    void naoDeveCriarEventoSeCategoriaNaoExiste() {
        EventoRequisicaoDto requisicao = criarEventoRequisicaoMock();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            eventoService.criarEvento(requisicao, UUID.randomUUID());
        });
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void deveInscreverParticipanteEmEventoComVaga() {
        Long eventoId = 1L;
        UUID participanteId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        Evento eventoMock = criarEventoMock(eventoId, organizerId, 2);

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));
        when(ingressosClient.createTicket(eventoId, participanteId, PaymentMethod.PIX)).thenReturn(null);

        eventoService.inscreverEmEvento(eventoId, participanteId);

        verify(eventoRepository).save(eventoMock);
        verify(ingressosClient).createTicket(eventoId, participanteId, PaymentMethod.PIX);
        assertThat(eventoMock.getParticipanteId()).contains(participanteId);
    }

    @Test
    void naoDeveInscreverEmEventoLotado() {
        Long eventoId = 1L;
        UUID organizerId = UUID.randomUUID();
        UUID participanteExistente = UUID.randomUUID();
        UUID novoParticipante = UUID.randomUUID();

        Evento eventoMock = criarEventoMock(eventoId, organizerId, 1);
        eventoMock.setParticipanteId(Set.of(participanteExistente));

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        var exception = assertThrows(IllegalStateException.class, () -> {
            eventoService.inscreverEmEvento(eventoId, novoParticipante);
        });
        assertThat(exception.getMessage()).isEqualTo("Evento com capacidade máxima atingida.");
        verify(eventoRepository, never()).save(any());
        verify(ingressosClient, never()).createTicket(anyLong(), any(),PaymentMethod.PIX);
    }

    @Test
    void deveDeletarEventoComSucesso() {
        Long eventoId = 1L;
        UUID organizerId = UUID.randomUUID();
        Evento eventoMock = criarEventoMock(eventoId, organizerId, 10);

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));
        doNothing().when(eventoRepository).delete(eventoMock);

        eventoService.deletarEvento(eventoId, organizerId);
        verify(eventoRepository).delete(eventoMock);
    }

    @Test
    void naoDeveDeletarEventoDeOutroOrganizador() {
        Long eventoId = 1L;
        UUID donoId = UUID.randomUUID();
        UUID invasorId = UUID.randomUUID();
        Evento eventoMock = criarEventoMock(eventoId, donoId, 10);

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        assertThrows(SecurityException.class, () -> {
            eventoService.deletarEvento(eventoId, invasorId);
        });
        verify(eventoRepository, never()).delete(any());
    }

    @Test
    void naoDeveDeletarEventoComParticipantesInscritos() {
        Long eventoId = 1L;
        UUID organizerId = UUID.randomUUID(); // Alterado para UUID
        UUID participanteExistente = UUID.randomUUID();
        Evento eventoMock = criarEventoMock(eventoId, organizerId, 10);
        eventoMock.setParticipanteId(Set.of(participanteExistente)); // Evento com 1 participante (UUID)

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        var exception = assertThrows(IllegalStateException.class, () -> {
            eventoService.deletarEvento(eventoId, organizerId);
        });
        assertThat(exception.getMessage()).isEqualTo("Não é possível excluir um evento com participantes inscritos.");
        verify(eventoRepository, never()).delete(any());
    }

    private EventoRequisicaoDto criarEventoRequisicaoMock() {
        EventoRequisicaoDto dto = new EventoRequisicaoDto();
        dto.setNome("Evento Teste");
        dto.setDescricao("Descrição do evento teste");
        dto.setLocalizacao("Online");
        dto.setData(LocalDateTime.now().plusDays(10));
        dto.setCapacidade(100);
        dto.setCategoriaId(1L);
        return dto;
    }

    private Evento criarEventoMock(Long eventoId, UUID organizerId, int capacidade) {
        Evento evento = new Evento();
        evento.setId(eventoId);
        evento.setOrganizerId(organizerId);
        evento.setCapacidade(capacidade);
        evento.setStatus(StatusEvento.ATIVO);
        evento.setParticipanteId(new HashSet<UUID>());

        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNome("Teste");
        evento.setCategoria(cat);

        return evento;
    }
}