/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.dto.ProductoMasVendidoDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.service.PdfService;
import com.example.msventa.service.VentaService;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(
        name = "Ventas",
        description = "Operaciones para realizar ventas, listar ventas y generar comprobantes PDF."
)
@RestController
@RequestMapping("/venta")
public class VentaController {

    private final com.example.msventa.service.CheckoutService checkout;
    private final VentaService ventaService;
    private final PdfService pdfService;
    private final LibroFeign libroFeign;

    public VentaController(VentaService ventaService, PdfService pdfService, LibroFeign libroFeign,
                           com.example.msventa.service.CheckoutService checkout) {
        this.checkout = checkout;
        this.ventaService = ventaService;
        this.pdfService = pdfService;
        this.libroFeign = libroFeign;
    }

    @Operation(
            summary = "Realizar venta",
            description = """
                    Registra una nueva venta a partir del carrito del usuario autenticado.
                    El token JWT permite identificar al usuario que realiza la compra.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta realizada correctamente."),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o no autorizado."),
            @ApiResponse(responseCode = "400", description = "No se pudo realizar la venta."),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/realizar")
    public ResponseEntity<Venta> realizarVenta(
        @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt,
        @RequestHeader("Idempotency-Key") String operation,
        @RequestParam String clienteNombre,
        @RequestParam(defaultValue="EFECTIVO") String metodoPago,
        @RequestParam(required=false) String nroOperacion,
        @RequestParam(required=false) Integer clienteId) {
        return ResponseEntity.ok(checkout.checkout(((Number)jwt.getClaim("id")).intValue(),jwt.getSubject(),operation,clienteNombre,metodoPago,nroOperacion,clienteId));
    }
    @GetMapping("/pendiente")
    public ResponseEntity<Venta> pendiente(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt){
        Venta pending=checkout.pending(((Number)jwt.getClaim("id")).intValue());
        return pending==null?ResponseEntity.noContent().build():ResponseEntity.ok(pending);
    }
    @PostMapping("/{id}/reintentar")
    public ResponseEntity<Venta> reintentar(@PathVariable Integer id,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt){return ResponseEntity.ok(checkout.retry(id,((Number)jwt.getClaim("id")).intValue()));}
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtener(@PathVariable Integer id){return ResponseEntity.of(java.util.Optional.ofNullable(ventaService.obtenerVentaPorId(id)));}

    @Operation(
            summary = "Top 5 productos más vendidos",
            description = "Agrega todos los VentaDetalle y devuelve los 5 productos con mayor cantidad vendida."
    )
    @GetMapping("/productos-mas-vendidos")
    public ResponseEntity<List<ProductoMasVendidoDto>> productosMasVendidos() {
        return ResponseEntity.ok(ventaService.getProductosMasVendidos());
    }

    @Operation(
            summary = "Listar ventas",
            description = """
                    Obtiene el registro completo de ventas. Cada detalle de venta intenta enriquecerse
                    con información del libro consultando el microservicio ms-book mediante Feign.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ventas obtenidas correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno al listar ventas.")
    })
    @GetMapping("/listar")
    public ResponseEntity<List<Venta>> listarVentas() {
        List<Venta> ventas = ventaService.listarVentas();
        completarInformacionLibros(ventas);
        return ResponseEntity.ok(ventas);
    }

    @Operation(
            summary = "Generar recibo PDF por venta",
            description = "Genera un recibo PDF asociado a una venta específica según su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recibo PDF generado correctamente."),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada."),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el PDF.")
    })
    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> generarRecibo(
            @Parameter(description = "ID de la venta para generar el recibo", required = true)
            @PathVariable Integer id
    ) {
        Venta venta = ventaService.obtenerVentaPorId(id);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] pdfBytes = pdfService.generarReciboPdf(venta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "recibo_" + id + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el recibo PDF de la venta: " + id,
                    e
            );
        }
    }

    @Operation(
            summary = "Generar reporte PDF de ventas",
            description = "Genera un archivo PDF con el registro general de ventas del sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte PDF generado correctamente."),
            @ApiResponse(responseCode = "500", description = "Error interno al generar el reporte PDF.")
    })
    @GetMapping("/registroVentasPdf")
    public ResponseEntity<byte[]> generarRegistroVentasPdf() {
        List<Venta> ventas = ventaService.listarVentas();
        completarInformacionLibros(ventas);

        try {
            byte[] pdfBytes = pdfService.generarRegistroVentasPdf(ventas);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "registro_ventas.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (DocumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo generar el reporte PDF de ventas.",
                    e
            );
        }
    }

    private void completarInformacionLibros(List<Venta> ventas) {
        for (Venta venta : ventas) for (VentaDetalle detalle : venta.getDetalles()) {
            if(detalle.getNombreProducto()!=null) continue;
            try {var response=libroFeign.listarLibro(detalle.getLibroId());if(response!=null&&response.getBody()!=null)detalle.setLibro(response.getBody());}
            catch(Exception ignored) { /* Preserve the historical sale when the catalog is unavailable. */ }
        }
    }
}
