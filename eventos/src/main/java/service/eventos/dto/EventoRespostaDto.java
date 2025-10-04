package service.eventos.dto;

import service.eventos.model.StatusEvento;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EventoRespostaDto {
    private Long id;
    private String nome;
    private String descricao;
    private String localizacao;
    private LocalDateTime data;
    private Integer capacidade;
    private Integer vagas;
    private StatusEvento status;
    private Long organizerId;
    private CategoriaDto categoria;
}
