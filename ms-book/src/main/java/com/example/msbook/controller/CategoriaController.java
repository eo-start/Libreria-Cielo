/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.dto.CategoriaRequestDto;
import com.example.msbook.entity.Categoria;
import com.example.msbook.service.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Categoria>> listarCategoriasAlias() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Categoria> listarCategoriaPorId(@PathVariable Integer id) {
        return categoriaService.listarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody CategoriaRequestDto categoriaRequestDto) {
        Categoria categoria = convertirDtoAEntidad(categoriaRequestDto);
        Categoria categoriaGuardada = categoriaService.guardar(categoria);

        return ResponseEntity.ok(categoriaGuardada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> editarCategoria(
            @PathVariable Integer id,
            @RequestBody CategoriaRequestDto categoriaRequestDto
    ) {
        Categoria categoria = convertirDtoAEntidad(categoriaRequestDto);
        Categoria categoriaEditada = categoriaService.editar(categoria, id);

        if (categoriaEditada == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(categoriaEditada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private Categoria convertirDtoAEntidad(CategoriaRequestDto categoriaRequestDto) {
        Categoria categoria = new Categoria();

        categoria.setNombre(categoriaRequestDto.getNombre());
        categoria.setClasificacion(categoriaRequestDto.getClasificacion());
        categoria.setFormato(categoriaRequestDto.getFormato());
        categoria.setIdioma(categoriaRequestDto.getIdioma());

        return categoria;
    }
}