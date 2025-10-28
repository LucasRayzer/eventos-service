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

    // Público
    @GetMapping
    public ResponseEntity<Page<EventoRespostaDto>> listarEventosDisponiveis(Pageable pageable) {
        return ResponseEntity.ok(eventoService.listarEventosDisponiveis(pageable));
    }

    // Público
    @GetMapping("/{id}")
    public ResponseEntity<EventoRespostaDto> buscarEventoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    // Fluxo CLIENTE

    @PostMapping("/{eventoId}/inscrever")
    public ResponseEntity<?> inscreverEmEvento(
            @PathVariable Long eventoId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "CLIENTE"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas CLIENTE pode se inscrever.");

        eventoService.inscreverEmEvento(eventoId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/minhas-inscricoes")
    public ResponseEntity<?> getMinhasInscricoes(
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "CLIENTE"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas CLIENTE pode consultar.");

        return ResponseEntity.ok(eventoService.buscarInscricoesDoParticipante(userId, pageable));
    }

    //Fluxo ORGANIZADOR

    @PostMapping("/criar-evento")
    public ResponseEntity<?> criarEvento(
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader(value = "X-User-Id", required = false) UUID organizerId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (organizerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "ORGANIZADOR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas ORGANIZADOR pode criar evento.");

        var eventoCriado = eventoService.criarEvento(requisicaoDto, organizerId);
        return new ResponseEntity<>(eventoCriado, HttpStatus.CREATED);
    }

    @PutMapping("/{eventoId}")
    public ResponseEntity<?> atualizarEvento(
            @PathVariable Long eventoId,
            @Valid @RequestBody EventoRequisicaoDto requisicaoDto,
            @RequestHeader(value = "X-User-Id", required = false) UUID organizerId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (organizerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "ORGANIZADOR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas ORGANIZADOR pode atualizar.");

        return ResponseEntity.ok(eventoService.atualizarEvento(eventoId, requisicaoDto, organizerId));
    }

    @DeleteMapping("/{eventoId}")
    public ResponseEntity<?> deletarEvento(
            @PathVariable Long eventoId,
            @RequestHeader(value = "X-User-Id", required = false) UUID organizerId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (organizerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "ORGANIZADOR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas ORGANIZADOR pode deletar.");

        eventoService.deletarEvento(eventoId, organizerId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/meus-eventos")
    public ResponseEntity<?> getMeusEventos(
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) UUID organizerId,
            @RequestHeader(value = "X-User-Roles", required = false) String rolesCsv
    ) {
        if (organizerId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado.");
        if (!hasRole(rolesCsv, "ORGANIZADOR"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas ORGANIZADOR pode consultar.");

        return ResponseEntity.ok(eventoService.buscarEventosDoOrganizador(organizerId, pageable));
    }

    // utilzinho local
    private boolean hasRole(String rolesCsv, String role) {
        if (rolesCsv == null || rolesCsv.isBlank()) return false;
        for (String r : rolesCsv.split(",")) {
            if (role.equalsIgnoreCase(r.trim())) return true;
        }
        return false;
    }
}
