/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.dto.ProvedoresRequestDto;
import com.example.msbook.entity.Provedores;
import com.example.msbook.service.ProvedoresService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Proveedores",
        description = "Operaciones para registrar, listar, editar y eliminar proveedores asociados al catálogo de libros."
)
@RestController
@RequestMapping("/provedores")
public class ProvedoresController {

    private final ProvedoresService provedoresService;

    public ProvedoresController(ProvedoresService provedoresService) {
        this.provedoresService = provedoresService;
    }

    @Operation(
            summary = "Listar proveedores",
            description = "Obtiene la lista completa de proveedores registrados en el microservicio de libros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedores obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public List<Provedores> listarProvedores() {
        return provedoresService.listar();
    }

    @Operation(
            summary = "Crear proveedor",
            description = "Registra un nuevo proveedor para asociarlo posteriormente al catálogo de libros."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor creado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para crear el proveedor."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping
    public Provedores crearProvedores(
            @Parameter(description = "Datos del proveedor que será registrado", required = true)
            @RequestBody ProvedoresRequestDto request
    ) {
        Provedores provedores = new Provedores();
        provedores.setNombre(request.getNombre());

        return provedoresService.guardar(provedores);
    }

    @Operation(
            summary = "Eliminar proveedor",
            description = "Elimina un proveedor registrado en el sistema según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/{id}")
    public String eliminarProvedores(
            @Parameter(description = "ID del proveedor que será eliminado", required = true)
            @PathVariable Integer id
    ) {
        provedoresService.eliminar(id);
        return "Se eliminó correctamente";
    }

    @Operation(
            summary = "Editar proveedor",
            description = "Actualiza el nombre de un proveedor existente según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Proveedor actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar el proveedor."),
            @ApiResponse(responseCode = "404", description = "Proveedor no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/{id}")
    public Provedores editarProvedores(
            @Parameter(description = "Datos actualizados del proveedor", required = true)
            @RequestBody ProvedoresRequestDto request,

            @Parameter(description = "ID del proveedor que será actualizado", required = true)
            @PathVariable Integer id
    ) {
        Provedores provedores = new Provedores();
        provedores.setNombre(request.getNombre());

        return provedoresService.editar(provedores, id);
    }
}