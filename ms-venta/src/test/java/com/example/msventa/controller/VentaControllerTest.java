/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.service.PdfService;
import com.example.msventa.service.VentaService;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    private static final String TOKEN_VALIDO = "Bearer token-valido";

    @Mock
    private com.example.msventa.service.CheckoutService checkout;
    @Mock
    private VentaService ventaService;

    @Mock
    private PdfService pdfService;

    @Mock
    private LibroFeign libroFeign;

    @InjectMocks
    private VentaController ventaController;

    private Venta venta;
    private LibroDto libroDto;

    @BeforeEach
    void setUp() {
        libroDto = new LibroDto();

        VentaDetalle detalle = new VentaDetalle();
        detalle.setId(1);
        detalle.setLibroId(10);
        detalle.setCantidad(2);
        detalle.setPrecio(50.0);

        venta = new Venta();
        venta.setId(1);
        venta.setUserId(1);
        venta.setUserName("admin");
        venta.setTotal(100.0);
        venta.setIgv(18.0);
        venta.setTotalConIgv(118.0);
        venta.setDetalles(List.of(detalle));
    }

    @Test
    void realizarVentaDebeRetornarVentaCreada() {
        var jwt=org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test").header("alg","HS256").claim("id",1).subject("admin").build();
        when(checkout.checkout(1,"admin","operation","Cliente","EFECTIVO",null,null)).thenReturn(venta);
        var response=ventaController.realizarVenta(jwt,"operation","Cliente","EFECTIVO",null,null);
        assertEquals(200,response.getStatusCode().value());assertSame(venta,response.getBody());
    }

    @Test
    void listarVentasDebeRetornarVentasConInformacionDeLibro() {
        when(ventaService.listarVentas()).thenReturn(List.of(venta));
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.ok(libroDto));

        ResponseEntity<List<Venta>> response = ventaController.listarVentas();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(libroDto, response.getBody().get(0).getDetalles().get(0).getLibro());

        verify(ventaService).listarVentas();
        verify(libroFeign).listarLibro(10);
    }

    @Test
    void listarVentasDebeRetornarVentasSinLibroCuandoFeignNoEsExitoso() {
        when(ventaService.listarVentas()).thenReturn(List.of(venta));
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        ResponseEntity<List<Venta>> response = ventaController.listarVentas();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNull(response.getBody().get(0).getDetalles().get(0).getLibro());

        verify(ventaService).listarVentas();
        verify(libroFeign).listarLibro(10);
    }

    @Test
    void generarReciboDebeRetornarPdfCuandoVentaExiste() throws DocumentException {
        byte[] pdfBytes = "PDF RECIBO".getBytes();

        when(ventaService.obtenerVentaPorId(1)).thenReturn(venta);
        when(pdfService.generarReciboPdf(venta)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = ventaController.generarRecibo(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertArrayEquals(pdfBytes, response.getBody());

        verify(ventaService).obtenerVentaPorId(1);
        verify(pdfService).generarReciboPdf(venta);
    }

    @Test
    void generarReciboDebeRetornarNotFoundCuandoVentaNoExiste() {
        when(ventaService.obtenerVentaPorId(99)).thenReturn(null);

        ResponseEntity<byte[]> response = ventaController.generarRecibo(99);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(ventaService).obtenerVentaPorId(99);
        verifyNoInteractions(pdfService);
    }

    @Test
    void generarReciboDebeLanzarResponseStatusExceptionCuandoPdfFalla() throws DocumentException {
        when(ventaService.obtenerVentaPorId(1)).thenReturn(venta);
        when(pdfService.generarReciboPdf(venta)).thenThrow(new DocumentException("Error PDF"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                this::ejecutarGenerarRecibo
        );

        assertEquals(500, exception.getStatusCode().value());
        assertNotNull(exception.getReason());
        assertTrue(exception.getReason().contains("No se pudo generar el recibo PDF de la venta: 1"));

        verify(ventaService).obtenerVentaPorId(1);
        verify(pdfService).generarReciboPdf(venta);
    }

    @Test
    void generarReporteVentasPdfDebeRetornarPdf() throws DocumentException {
        byte[] pdfBytes = "PDF REPORTE".getBytes();

        when(ventaService.listarVentas()).thenReturn(List.of(venta));
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.ok(libroDto));
        when(pdfService.generarRegistroVentasPdf(List.of(venta))).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = ventaController.generarRegistroVentasPdf();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertArrayEquals(pdfBytes, response.getBody());

        verify(ventaService).listarVentas();
        verify(libroFeign).listarLibro(10);
        verify(pdfService).generarRegistroVentasPdf(List.of(venta));
    }

    @Test
    void generarReporteVentasPdfDebeLanzarResponseStatusExceptionCuandoPdfFalla() throws DocumentException {
        when(ventaService.listarVentas()).thenReturn(List.of(venta));
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.ok(libroDto));
        when(pdfService.generarRegistroVentasPdf(List.of(venta))).thenThrow(new DocumentException("Error PDF"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                this::ejecutarGenerarReporteVentasPdf
        );

        assertEquals(500, exception.getStatusCode().value());
        assertEquals("No se pudo generar el reporte PDF de ventas.", exception.getReason());

        verify(ventaService).listarVentas();
        verify(libroFeign).listarLibro(10);
        verify(pdfService).generarRegistroVentasPdf(List.of(venta));
    }

    private void ejecutarGenerarRecibo() {
        ventaController.generarRecibo(1);
    }

    private void ejecutarGenerarReporteVentasPdf() {
        ventaController.generarRegistroVentasPdf();
    }
}
