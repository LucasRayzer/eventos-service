package service.eventos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.eventos.dto.EventoRequisicaoDto;
import service.eventos.exception.RecursoNaoEncontradoException;
import service.eventos.model.Categoria;
import service.eventos.model.Evento;
import service.eventos.repository.CategoriaRepository;
import service.eventos.repository.EventoRepository;
import service.eventos.service.EventoService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

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

    @InjectMocks
    private EventoService eventoService;

    @Test
    void deveCriarEventoComSucesso() {
        Long organizerId = 1L;
        EventoRequisicaoDto requisicao = criarEventoRequisicaoMock();
        Categoria categoria = new Categoria();
        categoria.setId(requisicao.getCategoriaId());

        when(categoriaRepository.findById(requisicao.getCategoriaId())).thenReturn(Optional.of(categoria));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var resposta = eventoService.criarEvento(requisicao, organizerId);

        assertThat(resposta).isNotNull();
        assertThat(resposta.getNome()).isEqualTo(requisicao.getNome());
        assertThat(resposta.getOrganizerId()).isEqualTo(organizerId);
        assertThat(resposta.getStatus()).isEqualTo(service.eventos.model.StatusEvento.ATIVO);
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void naoDeveCriarEventoSeCategoriaNaoExiste() {
        EventoRequisicaoDto requisicao = criarEventoRequisicaoMock();
        when(categoriaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> {
            eventoService.criarEvento(requisicao, 1L);
        });
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void deveInscreverParticipanteEmEventoComVaga() {
        Long eventoId = 1L;
        Long participanteId = 10L;
        Evento eventoMock = criarEventoMock(eventoId, 1L, 2); // Evento com capacidade 2

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        eventoService.inscreverEmEvento(eventoId, participanteId);

        verify(eventoRepository).save(eventoMock);
        assertThat(eventoMock.getParticipanteId()).contains(participanteId);
    }

    @Test
    void naoDeveInscreverEmEventoLotado() {
        Long eventoId = 1L;
        Evento eventoMock = criarEventoMock(eventoId, 1L, 1); // Capacidade 1
        eventoMock.setParticipanteId(Set.of(10L)); //Já tem 1 participante

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        var exception = assertThrows(IllegalStateException.class, () -> {
            eventoService.inscreverEmEvento(eventoId, 11L);
        });
        assertThat(exception.getMessage()).isEqualTo("Evento com capacidade máxima atingida.");
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void deveDeletarEventoComSucesso() {
        Long eventoId = 1L;
        Long organizerId = 5L;
        Evento eventoMock = criarEventoMock(eventoId, organizerId, 10);

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));
        doNothing().when(eventoRepository).delete(eventoMock);

        eventoService.deletarEvento(eventoId, organizerId);
        verify(eventoRepository).delete(eventoMock);
    }

    @Test
    void naoDeveDeletarEventoDeOutroOrganizador() {

        Long eventoId = 1L;
        Long donoId = 5L;
        Long invasorId = 99L;
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
        Long organizerId = 5L;
        Evento eventoMock = criarEventoMock(eventoId, organizerId, 10);
        eventoMock.setParticipanteId(Set.of(10L)); // Evento com 1 participante

        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoMock));

        var exception = assertThrows(IllegalStateException.class, () -> {
            eventoService.deletarEvento(eventoId, organizerId);
        });
        assertThat(exception.getMessage()).isEqualTo("Não é possível excluir um evento com participantes inscritos.");
        verify(eventoRepository, never()).delete(any());
    }

    // métodos auxiliares para criar mocks
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

    private Evento criarEventoMock(Long eventoId, Long organizerId, int capacidade) {
        Evento evento = new Evento();
        evento.setId(eventoId);
        evento.setOrganizerId(organizerId);
        evento.setCapacidade(capacidade);
        evento.setStatus(service.eventos.model.StatusEvento.ATIVO);
        evento.setParticipanteId(new java.util.HashSet<>());
        return evento;
    }
}