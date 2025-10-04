package service.eventos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriaRequisicaoDto {
    @NotBlank(message = "O nome da categoria não pode ser vazio.")
    private String nome;
}