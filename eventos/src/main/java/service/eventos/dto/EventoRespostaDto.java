package service.eventos.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import service.eventos.model.Evento;
import service.eventos.model.StatusEvento;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoRespostaDto {
    private Long id;
    private String nome;
    private String descricao;
    private String localizacao;
    private LocalDateTime data;
    private Integer capacidade;
    private Integer vagas;
    private StatusEvento status;
    private UUID organizerId;
    private String organizerNome;
    private CategoriaDto categoria;

    public EventoRespostaDto(Evento evento) {
        this.id = evento.getId();
        this.nome = evento.getNome();
        this.descricao = evento.getDescricao();
        this.localizacao = evento.getLocalizacao();
        this.organizerId = evento.getOrganizerId();
        this.status = evento.getStatus();
        this.data = evento.getData();
        this.capacidade = evento.getCapacidade();
        if (evento.getCapacidade() != null) {
            int inscritos = (evento.getParticipanteId() != null) ? evento.getParticipanteId().size() : 0;
            this.vagas = Math.max(0, evento.getCapacidade() - inscritos);
        } else {
            this.vagas = null;
        }
    }


}
