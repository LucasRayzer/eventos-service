package service.eventos.controller;


import service.eventos.client.UserClient;
import service.eventos.dto.*;
import service.eventos.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;
    private final UserClient userClient;

    // endpoint publico para listar todos os eventos disponíveis
    @GetMapping
    public ResponseEntity<Page<EventoRespostaDto>> listarEventosDisponiveis(Pageable pageable) {
        Page<EventoRespostaDto> eventos = eventoService.listarEventosDisponiveis(pageable);
        return ResponseEntity.ok(eventos);
    }
    /**
     * URL: POST /eventos/{eventoId}/inscrever?participanteId=X
     */
    @PostMapping("/{eventoId}/inscrever")
    public ResponseEntity<?> inscreverEmEvento(
            @PathVariable Long eventoId,
            @RequestParam("participanteId") UUID participanteId
    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(participanteId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"CLIENTE".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas participantes (CLIENTE) podem se inscrever.");
        }

        eventoService.inscreverEmEvento(eventoId, participanteId);
        return ResponseEntity.ok().build();
    }
    /**
     * URL: GET /eventos/minhas-inscricoes?participanteId=X
     */
    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<?> getMinhasInscricoes(
            Pageable pageable,
            @RequestParam("participanteId") UUID participanteId
    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(participanteId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"CLIENTE".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas participantes (CLIENTE) podem ver suas inscrições.");
        }

        Page<EventoRespostaDto> eventos = eventoService.buscarInscricoesDoParticipante(participanteId, pageable);
        return ResponseEntity.ok(eventos);
    }

    // Endpoints de Organizador
    /**
     * URL: POST /eventos/criar-evento?organizerId=X
     */
    @PostMapping("/criar-evento")
    public ResponseEntity<?> criarEvento(
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestParam("organizerId") UUID organizerId
    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(organizerId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"ORGANIZADOR".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (ORGANIZADOR) podem criar eventos.");
        }

        EventoRespostaDto eventoCriado = eventoService.criarEvento(requisicaoDto, organizerId);
        return new ResponseEntity<>(eventoCriado, HttpStatus.CREATED);
    }
    /**
     * URL: PUT /eventos/{eventoId}?organizerId=X
     */
    @PutMapping("/{eventoId}")
    public ResponseEntity<?> atualizarEvento(
            @PathVariable Long eventoId,
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestParam("organizerId") UUID organizerId

    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(organizerId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"ORGANIZADOR".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (ORGANIZADOR) podem atualizar eventos.");
        }

        EventoRespostaDto eventoAtualizado = eventoService.atualizarEvento(eventoId, requisicaoDto, organizerId);
        return ResponseEntity.ok(eventoAtualizado);
    }
    /**
     * URL: GET /eventos/meus-eventos?organizerId=X
     */
    @GetMapping("/meus-eventos")
    public ResponseEntity<?> getMeusEventos(
            Pageable pageable,
            @RequestParam("organizerId") UUID organizerId
    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(organizerId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"ORGANIZADOR".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (ORGANIZADOR) podem ver seus eventos.");
        }

        Page<EventoRespostaDto> eventos = eventoService.buscarEventosDoOrganizador(organizerId, pageable);
        return ResponseEntity.ok(eventos);
    }
    /**
     * URL: DELETE /eventos/{eventoId}?organizerId=X
     */
    @DeleteMapping("/{eventoId}")
    public ResponseEntity<?> deletarEvento(
            @PathVariable Long eventoId,
            @RequestParam("organizerId") UUID organizerId
    ) {
        UserClient.UserRespostaDto usuario = userClient.getUserById(organizerId);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        }
        if (!"ORGANIZADOR".equals(usuario.getTipo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado: Apenas organizadores (ORGANIZADOR) podem deletar eventos.");
        }

        eventoService.deletarEvento(eventoId, organizerId);
        return ResponseEntity.noContent().build();
    }
    //endpoint público
    @GetMapping("/{id}")
    public ResponseEntity<EventoRespostaDto> buscarEventoPorId(@PathVariable Long id) {
        EventoRespostaDto evento = eventoService.buscarPorId(id);
        return ResponseEntity.ok(evento);
    }
}