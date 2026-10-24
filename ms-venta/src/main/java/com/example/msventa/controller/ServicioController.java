/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.entity.Servicio;
import com.example.msventa.service.ServicioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/servicio")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @PostMapping("/guardar")
    public ResponseEntity<Servicio> guardar(@RequestBody Servicio servicio) {
        return ResponseEntity.ok(servicioService.guardar(servicio));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Servicio>> listar() {
        return ResponseEntity.ok(servicioService.listar());
    }
}
