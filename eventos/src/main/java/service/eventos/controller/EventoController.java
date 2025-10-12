package service.eventos.controller;


import service.eventos.dto.*;
import service.eventos.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    // endpoint publico para listar todos os eventos disponíveis
    @GetMapping
    public ResponseEntity<Page<EventoRespostaDto>> listarEventosDisponiveis(Pageable pageable) {
        Page<EventoRespostaDto> eventos = eventoService.listarEventosDisponiveis(pageable);
        return ResponseEntity.ok(eventos);
    }

    // endpoint para um participante se inscrever em um evento
    @PostMapping("/{eventoId}/inscrever")
    public ResponseEntity<Void> inscreverEmEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long participanteId
    ) {
        eventoService.inscreverEmEvento(eventoId, participanteId);
        return ResponseEntity.ok().build();
    }

    // endpoint para um participante ver suas inscrições
    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<Page<EventoRespostaDto>> getMinhasInscricoes(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long participanteId
    ) {
        Page<EventoRespostaDto> eventos = eventoService.buscarInscricoesDoParticipante(participanteId, pageable);
        return ResponseEntity.ok(eventos);
    }

    // endpoint para um organizador criar um evento
    @PostMapping
    public ResponseEntity<EventoRespostaDto> criarEvento(
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        EventoRespostaDto eventoCriado = eventoService.criarEvento(requisicaoDto, organizerId);
        return new ResponseEntity<>(eventoCriado, HttpStatus.CREATED);
    }

    // endpoint para o organizador atualizar um evento
    @PutMapping("/{eventoId}")
    public ResponseEntity<EventoRespostaDto> atualizarEvento(
            @PathVariable Long eventoId,
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        EventoRespostaDto eventoAtualizado = eventoService.atualizarEvento(eventoId, requisicaoDto, organizerId);
        return ResponseEntity.ok(eventoAtualizado);
    }

    // endpoint para um organizador ver os eventos que ele criou
    @GetMapping("/meus-eventos")
    public ResponseEntity<Page<EventoRespostaDto>> getMeusEventos(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        Page<EventoRespostaDto> eventos = eventoService.buscarEventosDoOrganizador(organizerId, pageable);
        return ResponseEntity.ok(eventos);
    }

    // endpoint para organizador deletar um evento
    @DeleteMapping("/{eventoId}")
    public ResponseEntity<Void> deletarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        eventoService.deletarEvento(eventoId, organizerId);
        return ResponseEntity.noContent().build();
    }
}