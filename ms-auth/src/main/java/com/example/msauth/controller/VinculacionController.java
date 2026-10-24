/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.controller;

import com.example.msauth.entity.AuthUser;
import com.example.msauth.service.VinculacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Vinculación de usuarios",
        description = "Operaciones para vincular usuarios con clientes o vendedores."
)
@RestController
@RequestMapping("/usuario")
public class VinculacionController {

    private final VinculacionService vinculacionService;

    public VinculacionController(VinculacionService vinculacionService) {
        this.vinculacionService = vinculacionService;
    }

    @PostMapping("/vincularCliente/{userId}/{clienteId}")
    public ResponseEntity<AuthUser> vincularCliente(
            @PathVariable Integer userId,
            @PathVariable Integer clienteId
    ) {
        return okOrBadRequest(vinculacionService.vincularCliente(userId, clienteId));
    }

    @PostMapping("/vincularVendedor/{userId}/{vendedorId}")
    public ResponseEntity<AuthUser> vincularVendedor(
            @PathVariable Integer userId,
            @PathVariable Integer vendedorId
    ) {
        return okOrBadRequest(vinculacionService.vincularVendedor(userId, vendedorId));
    }

    private ResponseEntity<AuthUser> okOrBadRequest(AuthUser authUser) {
        if (authUser == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authUser);
    }
}