/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.dto.CarritoItemRequestDto;
import com.example.msventa.entity.CarritoItem;
import com.example.msventa.exception.TokenAuthenticationException;
import com.example.msventa.feign.AuthFeign;
import com.example.msventa.service.CarritoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    private static final String TOKEN_VALIDO = "Bearer token-valido";

    @Mock
    private CarritoService carritoService;

    @Mock
    private AuthFeign authFeign;

    @InjectMocks
    private CarritoController carritoController;

    private CarritoItem carritoItem;
    private CarritoItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        carritoItem = new CarritoItem();

        requestDto = new CarritoItemRequestDto();
        requestDto.setLibroId(10);
        requestDto.setCantidad(2);
    }

    @Test
    void agregarItemDebeRetornarItemAgregadoCuandoTokenEsValido() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.ok(1));
        when(carritoService.agregarItem(1, 10, 2)).thenReturn(carritoItem);

        ResponseEntity<CarritoItem> response = carritoController.agregarItem(TOKEN_VALIDO, requestDto);

        assertEquals(200, response.getStatusCode().value());
        assertSame(carritoItem, response.getBody());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verify(carritoService).agregarItem(1, 10, 2);
    }

    @Test
    void eliminarItemDebeRetornarNoContentCuandoTokenEsValido() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.ok(1));
        doNothing().when(carritoService).eliminarItem(1, 5);

        ResponseEntity<Void> response = carritoController.eliminarItem(TOKEN_VALIDO, 5);

        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verify(carritoService).eliminarItem(1, 5);
    }

    @Test
    void listarItemsDebeRetornarListaCuandoTokenEsValido() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.ok(1));
        when(carritoService.listarItems(1)).thenReturn(List.of(carritoItem));

        ResponseEntity<List<CarritoItem>> response = carritoController.listarItems(TOKEN_VALIDO);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(carritoItem, response.getBody().get(0));

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verify(carritoService).listarItems(1);
    }

    @Test
    void agregarItemDebeLanzarExcepcionCuandoAuthFeignRetornaNull() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(null);

        TokenAuthenticationException exception = assertThrows(
                TokenAuthenticationException.class,
                this::ejecutarAgregarItem
        );

        assertEquals("No se pudo obtener el userId desde el token.", exception.getMessage());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verifyNoInteractions(carritoService);
    }

    @Test
    void agregarItemDebeLanzarExcepcionCuandoAuthFeignRetornaUnauthorized() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.status(401).build());

        TokenAuthenticationException exception = assertThrows(
                TokenAuthenticationException.class,
                this::ejecutarAgregarItem
        );

        assertEquals("No se pudo obtener el userId desde el token.", exception.getMessage());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verifyNoInteractions(carritoService);
    }

    @Test
    void agregarItemDebeLanzarExcepcionCuandoBodyEsNull() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.ok(null));

        TokenAuthenticationException exception = assertThrows(
                TokenAuthenticationException.class,
                this::ejecutarAgregarItem
        );

        assertEquals("No se pudo obtener el userId desde el token.", exception.getMessage());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verifyNoInteractions(carritoService);
    }

    @Test
    void eliminarItemDebeLanzarExcepcionCuandoTokenNoEsValido() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.status(401).build());

        TokenAuthenticationException exception = assertThrows(
                TokenAuthenticationException.class,
                this::ejecutarEliminarItem
        );

        assertEquals("No se pudo obtener el userId desde el token.", exception.getMessage());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verifyNoInteractions(carritoService);
    }

    @Test
    void listarItemsDebeLanzarExcepcionCuandoTokenNoEsValido() {
        when(authFeign.getUserId(TOKEN_VALIDO)).thenReturn(ResponseEntity.status(401).build());

        TokenAuthenticationException exception = assertThrows(
                TokenAuthenticationException.class,
                this::ejecutarListarItems
        );

        assertEquals("No se pudo obtener el userId desde el token.", exception.getMessage());

        verify(authFeign).getUserId(TOKEN_VALIDO);
        verifyNoInteractions(carritoService);
    }

    private void ejecutarAgregarItem() {
        carritoController.agregarItem(TOKEN_VALIDO, requestDto);
    }

    private void ejecutarEliminarItem() {
        carritoController.eliminarItem(TOKEN_VALIDO, 5);
    }

    private void ejecutarListarItems() {
        carritoController.listarItems(TOKEN_VALIDO);
    }
}