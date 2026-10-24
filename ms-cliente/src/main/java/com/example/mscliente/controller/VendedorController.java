/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.controller;

import com.example.mscliente.dto.VendedorRequestDto;
import com.example.mscliente.entity.Vendedor;
import com.example.mscliente.service.VendedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Vendedores",
        description = "Operaciones para registrar, listar, actualizar y eliminar vendedores."
)
@RestController
@RequestMapping("/vendedor")
public class VendedorController {

    private final VendedorService vendedorService;

    public VendedorController(VendedorService vendedorService) {
        this.vendedorService = vendedorService;
    }

    @Operation(
            summary = "Listar vendedores",
            description = "Obtiene la lista completa de vendedores registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedores obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public ResponseEntity<List<Vendedor>> listarVendedor() {
        return ResponseEntity.ok(vendedorService.listar());
    }

    @Operation(
            summary = "Registrar vendedor",
            description = "Registra un nuevo vendedor con sus datos personales y de contacto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para registrar el vendedor."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping
    public ResponseEntity<Vendedor> crearVendedor(
            @Parameter(description = "Datos del vendedor que será registrado", required = true)
            @Valid @RequestBody VendedorRequestDto request
    ) {
        Vendedor vendedor = convertirAEntidad(request);
        return ResponseEntity.ok(vendedorService.guardar(vendedor));
    }

    @Operation(
            summary = "Eliminar vendedor",
            description = "Elimina un vendedor registrado según su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarVendedor(
            @Parameter(description = "ID del vendedor que será eliminado", required = true)
            @PathVariable Integer id
    ) {
        vendedorService.eliminar(id);
        return ResponseEntity.ok("Se eliminó correctamente");
    }

    @Operation(
            summary = "Actualizar vendedor",
            description = "Actualiza la información personal y de contacto de un vendedor existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vendedor actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar el vendedor."),
            @ApiResponse(responseCode = "404", description = "Vendedor no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/{id}")
    public ResponseEntity<Vendedor> editarVendedor(
            @Parameter(description = "Datos actualizados del vendedor", required = true)
            @Valid @RequestBody VendedorRequestDto request,

            @Parameter(description = "ID del vendedor que será actualizado", required = true)
            @PathVariable Integer id
    ) {
        Vendedor vendedor = convertirAEntidad(request);
        return ResponseEntity.ok(vendedorService.editar(vendedor, id));
    }

    private Vendedor convertirAEntidad(VendedorRequestDto request) {
        Vendedor vendedor = new Vendedor();
        vendedor.setNombre(request.getNombre());
        vendedor.setDni(request.getDni());
        vendedor.setTelefono(request.getTelefono());
        vendedor.setCorreo(request.getCorreo());
        vendedor.setDireccion(request.getDireccion());
        return vendedor;
    }
}