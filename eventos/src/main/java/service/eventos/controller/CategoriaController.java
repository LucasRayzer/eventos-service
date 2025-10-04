package service.eventos.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.eventos.dto.CategoriaDto;
import service.eventos.dto.CategoriaRequisicaoDto;
import service.eventos.service.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaDto> criarCategoria(@Valid @RequestBody CategoriaRequisicaoDto requisicaoDto) {
        CategoriaDto categoriaCriada = categoriaService.criarCategoria(requisicaoDto);
        return new ResponseEntity<>(categoriaCriada, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDto>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }
}