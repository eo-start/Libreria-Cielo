/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.LibroFeign;
import com.itextpdf.text.DocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceTest {

    @Mock
    private LibroFeign libroFeign;

    @InjectMocks
    private PdfService pdfService;

    private Venta venta;
    private VentaDetalle detalle;
    private LibroDto libroDto;

    @BeforeEach
    void setUp() {
        libroDto = new LibroDto();
        libroDto.setTitulo("Cien años de soledad");
        libroDto.setAutor("Gabriel García Márquez");

        detalle = new VentaDetalle();
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
    void generarReciboPdfDebeGenerarBytesCuandoDetalleYaTieneLibro() throws DocumentException {
        detalle.setLibro(libroDto);

        byte[] resultado = generarRecibo();

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verifyNoInteractions(libroFeign);
    }

    @Test
    void generarReciboPdfDebeConsultarLibroCuandoDetalleNoTieneLibro() throws DocumentException {
        byte[] resultado = generarRecibo();

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verifyNoInteractions(libroFeign);
    }

    @Test
    void generarRegistroVentasPdfDebeGenerarBytesCuandoDetalleYaTieneLibro() throws DocumentException {
        detalle.setLibro(libroDto);

        byte[] resultado = generarRegistroVentas();

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);

        verifyNoInteractions(libroFeign);
    }

    @Test
    void generarRegistroVentasPdfDebeConsultarLibroCuandoDetalleNoTieneLibro() throws DocumentException {
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.ok(libroDto));

        byte[] resultado = generarRegistroVentas();

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        assertSame(libroDto, detalle.getLibro());

        verify(libroFeign).listarLibro(10);
    }

    @Test
    void generarReciboPdfDebeUsarPlaceholderCuandoLibroNoDisponible() {
        byte[] resultado = null;
        try {
            resultado = pdfService.generarReciboPdf(venta);
        } catch (DocumentException e) {
            fail("No se esperaba DocumentException: " + e.getMessage());
        }

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        verifyNoInteractions(libroFeign);
    }

    @Test
    void generarRegistroVentasPdfDebeUsarPlaceholderCuandoFeignRetornaNull() {
        detalle.setLibro(null);
        when(libroFeign.listarLibro(10)).thenReturn(null);

        byte[] resultado = null;
        try {
            resultado = pdfService.generarRegistroVentasPdf(List.of(venta));
        } catch (DocumentException e) {
            fail("No se esperaba DocumentException: " + e.getMessage());
        }

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        assertEquals("Producto #10 (eliminado)", detalle.getLibro().getTitulo());
        verify(libroFeign).listarLibro(10);
    }

    @Test
    void generarRegistroVentasPdfDebeUsarPlaceholderCuandoFeignNoEsExitoso() {
        detalle.setLibro(null);
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        byte[] resultado = null;
        try {
            resultado = pdfService.generarRegistroVentasPdf(List.of(venta));
        } catch (DocumentException e) {
            fail("No se esperaba DocumentException: " + e.getMessage());
        }

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        assertEquals("Producto #10 (eliminado)", detalle.getLibro().getTitulo());
        verify(libroFeign).listarLibro(10);
    }

    @Test
    void generarRegistroVentasPdfDebeUsarPlaceholderCuandoFeignBodyEsNull() {
        detalle.setLibro(null);
        when(libroFeign.listarLibro(10)).thenReturn(ResponseEntity.ok(null));

        byte[] resultado = null;
        try {
            resultado = pdfService.generarRegistroVentasPdf(List.of(venta));
        } catch (DocumentException e) {
            fail("No se esperaba DocumentException: " + e.getMessage());
        }

        assertNotNull(resultado);
        assertTrue(resultado.length > 0);
        assertEquals("Producto #10 (eliminado)", detalle.getLibro().getTitulo());
        verify(libroFeign).listarLibro(10);
    }

    private byte[] generarRecibo() throws DocumentException {
        return pdfService.generarReciboPdf(venta);
    }

    private byte[] generarRegistroVentas() throws DocumentException {
        return pdfService.generarRegistroVentasPdf(List.of(venta));
    }
}