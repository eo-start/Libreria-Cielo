/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.dto.LibroDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.entity.Venta;
import com.example.msventa.entity.VentaDetalle;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.feign.LibroFeign;
import com.example.msventa.repository.CarritoItemRepository;
import com.example.msventa.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;
    @Mock
    private CarritoItemRepository carritoItemRepository;
    @Mock
    private AuthFeign authFeign;
    @Mock
    private LibroFeign libroFeign;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private CarritoItem item;
    private LibroDto libro;

    @BeforeEach
    void setUp() {
        item = new CarritoItem();
        item.setId(1);
        item.setUserId(10);
        item.setLibroId(100);
        item.setCantidad(2);

        libro = new LibroDto();
        libro.setId(100);
        libro.setTitulo("Clean Code");
        libro.setAutor("Robert C. Martin");
        libro.setStock(10);
        libro.setPrecio(50.0);
    }









    @Test
    @DisplayName("Debe listar ventas")
    void listarVentas_debeRetornarLista() {
        Venta venta = new Venta();
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        List<Venta> resultado = ventaService.listarVentas();

        assertEquals(1, resultado.size());
        verify(ventaRepository).findAll();
    }

    @Test
    @DisplayName("Debe obtener venta por id cuando existe")
    void obtenerVentaPorId_debeRetornarVenta() {
        Venta venta = new Venta();
        venta.setId(1);
        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));

        Venta resultado = ventaService.obtenerVentaPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
    }

    @Test
    @DisplayName("Debe retornar null cuando venta no existe")
    void obtenerVentaPorId_debeRetornarNull() {
        when(ventaRepository.findById(99)).thenReturn(Optional.empty());

        Venta resultado = ventaService.obtenerVentaPorId(99);

        assertNull(resultado);
    }

    @Test
    @DisplayName("Debe obtener venta con detalles y enriquecer libros")
    void obtenerVentaConDetalles_debeCompletarInformacionLibro() {
        Venta venta = new Venta();
        VentaDetalle detalle = new VentaDetalle();
        detalle.setLibroId(100);
        detalle.setCantidad(2);
        detalle.setPrecio(50.0);
        detalle.setVenta(venta);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.ok(libro));

        Venta resultado = ventaService.obtenerVentaConDetalles(1);

        assertNotNull(resultado);
        assertEquals("Clean Code", resultado.getDetalles().get(0).getLibro().getTitulo());
        verify(libroFeign).listarLibro(100);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando venta no existe")
    void obtenerVentaConDetalles_debeLanzarExcepcionCuandoNoExisteVenta() {
        when(ventaRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaConDetalles(99));

        assertEquals("Venta no encontrada", ex.getMessage());
    }





    @Test
    @DisplayName("Debe lanzar excepción cuando libro no se puede obtener al consultar detalle")
    void obtenerVentaConDetalles_debeLanzarExcepcionCuandoLibroNoDisponible() {
        Venta venta = new Venta();
        VentaDetalle detalle = new VentaDetalle();
        detalle.setLibroId(100);
        detalle.setVenta(venta);
        venta.getDetalles().add(detalle);

        when(ventaRepository.findById(1)).thenReturn(Optional.of(venta));
        when(libroFeign.listarLibro(100)).thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaConDetalles(1));

        assertTrue(ex.getMessage().contains("100") && ex.getMessage().contains("catalogo"));
    }
}
