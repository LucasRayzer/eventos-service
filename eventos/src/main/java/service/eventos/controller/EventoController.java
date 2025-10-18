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

    @PostMapping("/{eventoId}/inscrever")
    public ResponseEntity<?> inscreverEmEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long participanteId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //verificação de permissão
        if (userRole != 2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas participantes (tipo 2) podem se inscrever.");
        }
        eventoService.inscreverEmEvento(eventoId, participanteId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<?> getMinhasInscricoes(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long participanteId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //Verificação de permissão
        if (userRole != 2) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas participantes (tipo 2) podem ver suas inscrições.");
        }
        Page<EventoRespostaDto> eventos = eventoService.buscarInscricoesDoParticipante(participanteId, pageable);
        return ResponseEntity.ok(eventos);
    }

    // Endpoints de Organizador
    @PostMapping("/{criar-evento}")
    public ResponseEntity<?> criarEvento(
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader("X-User-ID") Long organizerId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //Verificação de permissão
        if (userRole != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (tipo 1) podem criar eventos.");
        }
        EventoRespostaDto eventoCriado = eventoService.criarEvento(requisicaoDto, organizerId);
        return new ResponseEntity<>(eventoCriado, HttpStatus.CREATED);
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<?> atualizarEvento(
            @PathVariable Long eventoId,
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader("X-User-ID") Long organizerId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //erificação de permissão
        if (userRole != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (tipo 1) podem atualizar eventos.");
        }
        EventoRespostaDto eventoAtualizado = eventoService.atualizarEvento(eventoId, requisicaoDto, organizerId);
        return ResponseEntity.ok(eventoAtualizado);
    }

    @GetMapping("/meus-eventos")
    public ResponseEntity<?> getMeusEventos(
            Pageable pageable,
            @RequestHeader("X-User-ID") Long organizerId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //Verificação de permissão
        if (userRole != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (tipo 1) podem ver seus eventos.");
        }
        Page<EventoRespostaDto> eventos = eventoService.buscarEventosDoOrganizador(organizerId, pageable);
        return ResponseEntity.ok(eventos);
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<?> deletarEvento(
            @PathVariable Long eventoId,
            @RequestHeader("X-User-ID") Long organizerId,
            @RequestHeader("X-User-Role") Integer userRole
    ) {
        //Verificação de permissão
        if (userRole != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (tipo 1) podem deletar eventos.");
        }
        eventoService.deletarEvento(eventoId, organizerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoRespostaDto> buscarEventoPorId(@PathVariable Long id) {
        EventoRespostaDto evento = eventoService.buscarPorId(id);
        return ResponseEntity.ok(evento);
    }
}