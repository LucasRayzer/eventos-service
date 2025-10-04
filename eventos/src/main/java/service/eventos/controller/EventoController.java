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

    @PostMapping("/criar-evento")
    public ResponseEntity<EventoRespostaDto> criarEvento(
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        EventoRespostaDto eventoCriado = eventoService.criarEvento(requisicaoDto, organizerId);
        return new ResponseEntity<>(eventoCriado, HttpStatus.CREATED);
    }

    @GetMapping("/meus-eventos")
    public ResponseEntity<Page<EventoRespostaDto>> getMeusEventos(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        Page<EventoRespostaDto> eventos = eventoService.buscarEventosDoOrganizador(organizerId, pageable);
        return ResponseEntity.ok(eventos);
    }

    @DeleteMapping("/meus-eventos/{eventoId}")
    public ResponseEntity<Void> deletarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long organizerId
    ) {
        eventoService.deletarEvento(eventoId, organizerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventoId}/inscrever")
    public ResponseEntity<Void> inscreverEmEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long participanteId
    ) {
        eventoService.inscreverEmEvento(eventoId, participanteId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<Page<EventoRespostaDto>> getMinhasInscricoes(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long participanteId
    ) {
        Page<EventoRespostaDto> eventos = eventoService.buscarInscricoesDoParticipante(participanteId, pageable);
        return ResponseEntity.ok(eventos);
    }
}