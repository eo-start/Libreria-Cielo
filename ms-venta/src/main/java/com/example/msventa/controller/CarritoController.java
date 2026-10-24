/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.dto.CarritoItemRequestDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.exception.TokenAuthenticationException;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.service.CarritoService;
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
        name = "Carrito de compras",
        description = "Operaciones para agregar, listar y eliminar productos del carrito del usuario autenticado."
)
@RestController
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final AuthFeign authFeign;

    public CarritoController(CarritoService carritoService, AuthFeign authFeign) {
        this.carritoService = carritoService;
        this.authFeign = authFeign;
    }

    @Operation(
            summary = "Agregar libro al carrito",
            description = """
                    Agrega un libro al carrito de compras del usuario autenticado.
                    El usuario se obtiene a partir del token JWT enviado en el encabezado Authorization.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto agregado correctamente al carrito."),
            @ApiResponse(responseCode = "400", description = "Datos inválidos para agregar el producto."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o no autorizado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/agregar")
    public ResponseEntity<CarritoItem> agregarItem(
            @Parameter(description = "Token JWT en formato Bearer", required = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "Datos del producto que será agregado al carrito", required = true)
            @Valid @RequestBody CarritoItemRequestDto item
    ) {
        Integer userId = obtenerUserIdDesdeToken(token);
        CarritoItem nuevoItem = carritoService.agregarItem(userId, item.getLibroId(), item.getCantidad());
        return ResponseEntity.ok(nuevoItem);
    }

    @Operation(
            summary = "Eliminar item del carrito",
            description = "Elimina un item del carrito de compras del usuario autenticado según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item eliminado correctamente del carrito."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o no autorizado."),
            @ApiResponse(responseCode = "404", description = "Item no encontrado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarItem(
            @Parameter(description = "Token JWT en formato Bearer", required = true)
            @RequestHeader("Authorization") String token,

            @Parameter(description = "ID del item del carrito que será eliminado", required = true)
            @PathVariable Integer id
    ) {
        Integer userId = obtenerUserIdDesdeToken(token);
        carritoService.eliminarItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Listar carrito del usuario autenticado",
            description = "Obtiene todos los items del carrito asociados al usuario autenticado mediante token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito obtenido correctamente."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o no autorizado."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/listar")
    public ResponseEntity<List<CarritoItem>> listarItems(
            @Parameter(description = "Token JWT en formato Bearer", required = true)
            @RequestHeader("Authorization") String token
    ) {
        Integer userId = obtenerUserIdDesdeToken(token);
        List<CarritoItem> items = carritoService.listarItems(userId);
        return ResponseEntity.ok(items);
    }

    private Integer obtenerUserIdDesdeToken(String token) {
        ResponseEntity<Integer> response = authFeign.getUserId(token);

        if (response == null || !response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new TokenAuthenticationException("No se pudo obtener el userId desde el token.");
        }

        return response.getBody();
    }
}