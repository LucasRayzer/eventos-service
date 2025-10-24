package service.eventos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import service.eventos.commons.PaymentMethod;

@Data
public class TicketReserveRequestDto {
    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    @Positive
    private Long participantId;


    private service.eventos.commons.PaymentMethod method;
}
