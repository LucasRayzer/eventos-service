package service.eventos.service;

import service.eventos.dto.*;
import service.eventos.exception.RecursoNaoEncontradoException;
import service.eventos.model.*;
import service.eventos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public EventoRespostaDto criarEvento(EventoRequisicaoDto requisicaoDto, Long organizerId) {
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

    @Transactional
    public void inscreverEmEvento(Long eventoId, Long participanteId) {
        Evento evento = buscarEventoPorId(eventoId);

        if (evento.getParticipanteId().size() >= evento.getCapacidade()) {
            throw new IllegalStateException("Evento com capacidade máxima atingida.");
        }
        if (evento.getStatus() != StatusEvento.ATIVO) {
            throw new IllegalStateException("Só é possível se inscrever em eventos ativos.");
        }

        evento.getParticipanteId().add(participanteId);
        eventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public Page<EventoRespostaDto> buscarEventosDoOrganizador(Long organizerId, Pageable pageable) {
        Page<Evento> eventos = eventoRepository.findByOrganizerId(organizerId, pageable);
        return eventos.map(this::paraRespostaDto);
    }

    @Transactional(readOnly = true)
    public Page<EventoRespostaDto> buscarInscricoesDoParticipante(Long participanteId, Pageable pageable) {
        Page<Evento> eventos = eventoRepository.findByParticipanteIdContains(participanteId, pageable);
        return eventos.map(this::paraRespostaDto);
    }

    @Transactional
    public void deletarEvento(Long eventoId, Long organizerId) {
        Evento evento = buscarEventoPorId(eventoId);

        if (!evento.getOrganizerId().equals(organizerId)) {
            throw new SecurityException("Apenas o organizador pode excluir o evento.");
        }
        if (!evento.getParticipanteId().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um evento com participantes inscritos.");
        }

        eventoRepository.delete(evento);
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

        return dto;
    }

    public EventoRespostaDto buscarPorId(Long id) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado com ID: " + id));

        return new EventoRespostaDto(evento);
    }
}