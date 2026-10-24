/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.entity.MovimientoInventario;
import com.example.msbook.repository.MovimientoInventarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/libros/movimiento")
public class MovimientoInventarioController {

    private final MovimientoInventarioRepository repo;

    public MovimientoInventarioController(MovimientoInventarioRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/guardar")
    public ResponseEntity<MovimientoInventario> guardar(@RequestBody MovimientoInventario movimiento) {
        movimiento.setFecha(new Date());
        return ResponseEntity.ok(repo.save(movimiento));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<MovimientoInventario>> listar() {
        return ResponseEntity.ok(repo.findTop10ByOrderByFechaDesc());
    }
}
