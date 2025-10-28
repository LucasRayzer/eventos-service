package service.eventos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import service.eventos.commons.PaymentMethod;

import java.util.UUID;

@Data
public class TicketReserveRequestDto {
    @NotNull
    @Positive
    private Long eventId;

    @NotNull
    private UUID participantId;


    private service.eventos.commons.PaymentMethod method;
}
