package service.eventos.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import service.eventos.commons.PaymentMethod;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Cliente HTTP para o serviço de Ingressos.
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
     */
    public TicketCreateResponse createTicket(Long eventId, UUID participantId, PaymentMethod method) {
        String url = baseUrl + "/tickets/reserve";
        TicketCreateRequest body = new TicketCreateRequest(eventId, participantId, method);
        return rest.postForObject(url, body, TicketCreateResponse.class);
    }

    // dto para recuperar dados do serviço de ingresso

    @Data
    public static class TicketCreateRequest {
        private Long eventId;
        private UUID participantId;
        private PaymentMethod method;

        public TicketCreateRequest(Long eventId, UUID participantId, PaymentMethod method) {
            this.eventId = eventId;
            this.participantId = participantId;
            this.method = method;
        }
    }

    @Data
    public static class TicketCreateResponse {
        private Long ticketId;
        private String code;
        private String status;
        private OffsetDateTime expiresAt;
    }
}
