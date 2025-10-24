package service.eventos.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;

/**
 * Cliente HTTP para o serviço de Ingressos.
 * Espera que a base esteja em services.tickets.base-url (ex.: http://ingressos-service:8081).
 */
@Component
public class IngressosClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public IngressosClient(@Value("${services.tickets.base-url}") String baseUrl) {
        this.rest = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    /**
     * Cria um ticket (1 ingresso) para um participante em um evento.
     * POST {baseUrl}/tickets
     */
    public TicketCreateResponse createTicket(Long eventId, Long participantId, String method) {
        String url = baseUrl + "/tickets/reserve";
        TicketCreateRequest body = new TicketCreateRequest(eventId, participantId, method);
        return rest.postForObject(url, body, TicketCreateResponse.class);
    }

    // ---------- DTOs (locais ao client, para desacoplar contratos) ----------

    @Data
    public static class TicketCreateRequest {
        private Long eventId;
        private Long participantId;
        private String method; // "PIX", "CARTAO" (opcional)

        public TicketCreateRequest(Long eventId, Long participantId, String method) {
            this.eventId = eventId;
            this.participantId = participantId;
            this.method = method;
        }
    }

    @Data
    public static class TicketCreateResponse {
        private Long ticketId;
        private String code;
        private String status;              // RESERVED, CONFIRMED, ...
        private OffsetDateTime expiresAt;   // pode vir null se não houver TTL
    }
}
