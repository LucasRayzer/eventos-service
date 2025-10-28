package service.eventos.service;

import service.eventos.client.IngressosClient;
import service.eventos.client.UserClient;
import service.eventos.commons.PaymentMethod;
import service.eventos.dto.*;
import service.eventos.exception.RecursoNaoEncontradoException;
import service.eventos.model.*;
import service.eventos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;
    private final IngressosClient ingressosClient;
    private final UserClient userClient;

    //MÉTODOS PARA ORGANIZADORES
    @Transactional
    public EventoRespostaDto criarEvento(EventoRequisicaoDto requisicaoDto, UUID organizerId) {
        Categoria categoria = buscarCategoriaPorId(requisicaoDto.getCategoriaId());

        Evento evento = new Evento();
        evento.setNome(requisicaoDto.getNome());
        evento.setDescricao(requisicaoDto.getDescricao());
        evento.setLocalizacao(requisicaoDto.getLocalizacao());
        evento.setData(requisicaoDto.getData());
        evento.setCapacidade(requisicaoDto.getCapacidade());
        evento.setCategoria(categoria);
        evento.setOrganizerId(organizerId);
        evento.setStatus(StatusEvento.ATIVO);

        Evento eventoSalvo = eventoRepository.save(evento);
        return paraRespostaDto(eventoSalvo);
    }

    // funcionalidade de Atualizar Evento
    @Transactional
    public EventoRespostaDto atualizarEvento(Long eventoId, EventoRequisicaoDto requisicaoDto, UUID organizerId) {
        Evento eventoExistente = buscarEventoPorId(eventoId);

        // apenas o dono do evento pode atualizar
        if (!eventoExistente.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("Apenas o organizador pode atualizar o evento.");
        }

        Categoria categoria = buscarCategoriaPorId(requisicaoDto.getCategoriaId());

        eventoExistente.setNome(requisicaoDto.getNome());
        eventoExistente.setDescricao(requisicaoDto.getDescricao());
        eventoExistente.setLocalizacao(requisicaoDto.getLocalizacao());
        eventoExistente.setData(requisicaoDto.getData());
        eventoExistente.setCapacidade(requisicaoDto.getCapacidade());
        eventoExistente.setCategoria(categoria);

        Evento eventoAtualizado = eventoRepository.save(eventoExistente);
        return paraRespostaDto(eventoAtualizado);
    }

    @Transactional
    public void deletarEvento(Long eventoId, UUID organizerId) {
        Evento evento = buscarEventoPorId(eventoId);

        // apenas o dono pode excluir
        if (!evento.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("Apenas o organizador pode excluir o evento.");
        }
        // não pode excluir se tiver inscritos
        if (!evento.getParticipanteId().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um evento com participantes inscritos.");
        }

        eventoRepository.delete(evento);
    }

    @Transactional(readOnly = true)
    public Page<EventoRespostaDto> buscarEventosDoOrganizador(UUID organizerId, Pageable pageable) {
        return eventoRepository.findByOrganizerId(organizerId, pageable).map(this::paraRespostaDto);
    }

    // MÉTODOS PARA PARTICIPANTES
    @Transactional
    public void inscreverEmEvento(Long eventoId, UUID participanteId) {
        Evento evento = buscarEventoPorId(eventoId);

        if (evento.getParticipanteId().contains(participanteId)) {
            throw new IllegalStateException("Usuário já inscrito neste evento.");
        }

        if (evento.getParticipanteId().size() >= evento.getCapacidade()) {
            throw new IllegalStateException("Evento com capacidade máxima atingida.");
        }
        if (evento.getStatus() != StatusEvento.ATIVO) {
            throw new IllegalStateException("Só é possível se inscrever em eventos ativos.");
        }

        evento.getParticipanteId().add(participanteId);
        eventoRepository.save(evento);
        ingressosClient.createTicket(eventoId, participanteId, PaymentMethod.PIX);
    }

    @Transactional(readOnly = true)
    public Page<EventoRespostaDto> buscarInscricoesDoParticipante(UUID participanteId, Pageable pageable) {
        return eventoRepository.findByParticipanteIdContains(participanteId, pageable).map(this::paraRespostaDto);
    }


    // para o participante visualizar todos os eventos disponíveis
    @Transactional(readOnly = true)
    public Page<EventoRespostaDto> listarEventosDisponiveis(Pageable pageable) {
        return eventoRepository.findByStatus(StatusEvento.ATIVO, pageable).map(this::paraRespostaDto);
    }


    private Evento buscarEventoPorId(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com ID: " + eventoId));
    }

    private Categoria buscarCategoriaPorId(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com ID: " + categoriaId));
    }
    private EventoRespostaDto paraRespostaDto(Evento evento) {
        EventoRespostaDto dto = new EventoRespostaDto();
        dto.setId(evento.getId());
        dto.setNome(evento.getNome());
        dto.setDescricao(evento.getDescricao());
        dto.setLocalizacao(evento.getLocalizacao());
        dto.setData(evento.getData());
        dto.setCapacidade(evento.getCapacidade());
        dto.setStatus(evento.getStatus());
        dto.setOrganizerId(evento.getOrganizerId());
        dto.setVagas(evento.getCapacidade() - evento.getParticipanteId().size());

        CategoriaDto categoriaDto = new CategoriaDto();
        categoriaDto.setId(evento.getCategoria().getId());
        categoriaDto.setNome(evento.getCategoria().getNome());
        dto.setCategoria(categoriaDto);

        // pega o dto do usuário
        try {
            UserClient.UserRespostaDto organizador = userClient.getUserById(evento.getOrganizerId());
            if (organizador != null) {
                dto.setOrganizerNome(organizador.getNome());
            } else {
                dto.setOrganizerNome("Organizador não encontrado");
            }
        } catch (Exception e) {
            // Em caso de falha (ex: user-service offline),
            // não quebramos a requisição inteira
            dto.setOrganizerNome("Nome indisponível (serviço offline)");
        }

        return dto;
    }

    public EventoRespostaDto buscarPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com ID: " + id));

        return new EventoRespostaDto(evento);
    }
}