/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.CarritoItem;
import com.example.msventa.repository.CarritoItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private com.example.msventa.repository.VentaRepository ventaRepository;
    @Mock
    private CarritoItemRepository carritoItemRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private CarritoItem item;

    @BeforeEach
    void setUp() {
        item = new CarritoItem();
        item.setId(1);
        item.setUserId(10);
        item.setLibroId(100);
        item.setCantidad(2);
    }

    @Test
    @DisplayName("Debe agregar item al carrito correctamente")
    void agregarItem_debeGuardarYRetornarItem() {
        when(carritoItemRepository.save(any(CarritoItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CarritoItem resultado = carritoService.agregarItem(10, 100, 2);

        assertNotNull(resultado);
        assertEquals(10, resultado.getUserId());
        assertEquals(100, resultado.getLibroId());
        assertEquals(2, resultado.getCantidad());
        verify(carritoItemRepository).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Debe eliminar item cuando pertenece al usuario")
    void eliminarItem_debeEliminarCuandoUsuarioCoincide() {
        when(carritoItemRepository.findById(1)).thenReturn(Optional.of(item));

        carritoService.eliminarItem(10, 1);

        verify(carritoItemRepository).delete(item);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando item no existe")
    void eliminarItem_debeLanzarExcepcionCuandoNoExiste() {
        when(carritoItemRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> carritoService.eliminarItem(10, 99));

        assertEquals("Item no encontrado", ex.getMessage());
        verify(carritoItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando usuario no tiene permiso")
    void eliminarItem_debeLanzarExcepcionCuandoUsuarioNoCoincide() {
        when(carritoItemRepository.findById(1)).thenReturn(Optional.of(item));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> carritoService.eliminarItem(99, 1));

        assertEquals("Acceso denegado", ex.getMessage());
        verify(carritoItemRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe listar items por usuario")
    void listarItems_debeRetornarLista() {
        when(carritoItemRepository.findByUserId(10)).thenReturn(List.of(item));

        List<CarritoItem> resultado = carritoService.listarItems(10);

        assertEquals(1, resultado.size());
        assertEquals(item, resultado.get(0));
        verify(carritoItemRepository).findByUserId(10);
    }
}
