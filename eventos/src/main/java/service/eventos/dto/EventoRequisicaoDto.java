package service.eventos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoRequisicaoDto {

    @NotBlank(message = "O nome não pode ser vazio.")
    private String nome;

    @NotBlank(message = "A descrição não pode ser vazia.")
    private String descricao;

    @NotBlank(message = "A localização não pode ser vazia.")
    private String localizacao;

    @NotNull(message = "A data e horário são obrigatórios.")
    @Future(message = "A data do evento não pode ser no passado.")
    private LocalDateTime data;

    @NotNull(message = "A capacidade é obrigatória.")
    @Min(value = 1, message = "A capacidade mínima é 1.")
    private Integer capacidade;

    @NotNull(message = "O ID da categoria é obrigatório.")
    private Long categoriaId;
}