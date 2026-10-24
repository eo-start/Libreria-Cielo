/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.controller;

import com.example.mscliente.dto.ClienteRequestDto;
import com.example.mscliente.entity.Cliente;
import com.example.mscliente.service.ClienteService;
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
        name = "Clientes",
        description = "Operaciones para registrar, listar, buscar, actualizar y eliminar clientes."
)
@RestController
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(
            summary = "Listar clientes",
            description = "Obtiene la lista completa de clientes registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clientes obtenidos correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.listar());
    }

    @Operation(
            summary = "Registrar cliente",
            description = "Registra un nuevo cliente con sus datos personales y de contacto."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente registrado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para registrar el cliente."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping
    public ResponseEntity<Cliente> guardar(
            @Parameter(description = "Datos del cliente que será registrado", required = true)
            @Valid @RequestBody ClienteRequestDto request
    ) {
        Cliente cliente = convertirAEntidad(request);
        return ResponseEntity.ok(clienteService.guardar(cliente));
    }

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Obtiene la información de un cliente específico según su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado correctamente."),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(
            @Parameter(description = "ID del cliente consultado", required = true)
            @PathVariable Integer id
    ) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @Operation(
            summary = "Actualizar cliente",
            description = "Actualiza la información personal y de contacto de un cliente existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para actualizar el cliente."),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> editar(
            @Parameter(description = "ID del cliente que será actualizado", required = true)
            @PathVariable Integer id,

            @Parameter(description = "Datos actualizados del cliente", required = true)
            @Valid @RequestBody ClienteRequestDto request
    ) {
        Cliente cliente = convertirAEntidad(request);
        cliente.setId(id);
        return ResponseEntity.ok(clienteService.editar(cliente));
    }

    @Operation(
            summary = "Eliminar cliente",
            description = "Elimina un cliente registrado según su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente eliminado correctamente."),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(
            @Parameter(description = "ID del cliente que será eliminado", required = true)
            @PathVariable Integer id
    ) {
        clienteService.eliminar(id);
        return ResponseEntity.ok("Eliminación completa");
    }

    private Cliente convertirAEntidad(ClienteRequestDto request) {
        Cliente cliente = new Cliente();
        cliente.setNombreCompleto(request.getNombreCompleto());
        cliente.setDni(request.getDni());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setEstado("ACTIVO");
        return cliente;
    }
}