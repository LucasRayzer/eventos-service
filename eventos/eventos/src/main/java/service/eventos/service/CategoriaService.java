package service.eventos.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.eventos.dto.CategoriaDto;
import service.eventos.dto.CategoriaRequisicaoDto;
import service.eventos.model.Categoria;
import service.eventos.repository.CategoriaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    @Transactional
    public CategoriaDto criarCategoria(CategoriaRequisicaoDto requisicaoDto) {
        Categoria novaCategoria = new Categoria();
        novaCategoria.setNome(requisicaoDto.getNome());
        Categoria categoriaSalva = categoriaRepository.save(novaCategoria);
        return paraDto(categoriaSalva);
    }
    @Transactional
    public List<CategoriaDto> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::paraDto)
                .collect(Collectors.toList());
    }
    @Transactional
    private CategoriaDto paraDto(Categoria categoria) {
        CategoriaDto dto = new CategoriaDto();
        dto.setId(categoria.getId());
        dto.setNome(categoria.getNome());
        return dto;
    }
}
