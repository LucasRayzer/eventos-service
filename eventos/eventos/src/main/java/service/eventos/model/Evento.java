package service.eventos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime; // ALTERADO para incluir horário
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "eventos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private String localizacao;

    @Column(nullable = false)
    private LocalDateTime data;

    @Column(nullable = false)
    private Integer capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEvento status;

    @Column(nullable = false)
    private Long organizerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "evento_participantes", joinColumns = @JoinColumn(name = "evento_id"))
    @Column(name = "participante_id")
    private Set<Long> participanteId = new HashSet<>();
}
