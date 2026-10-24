/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import com.example.msventa.entity.Servicio;
import com.example.msventa.service.ServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioControllerTest {

    private static final Date FECHA_FIJA = new Date(0L);

    @Mock
    private ServicioService servicioService;

    @InjectMocks
    private ServicioController servicioController;

    private Servicio servicio;

    @BeforeEach
    void setUp() {
        servicio = new Servicio();
        servicio.setId(1);
        servicio.setTipoServicio("IMPRESION");
        servicio.setCliente("Juan Pérez");
        servicio.setCantidad(10);
        servicio.setPrecioUnitario(0.20);
        servicio.setTotal(2.00);
        servicio.setMetodoPago("EFECTIVO");
        servicio.setFecha(FECHA_FIJA);
    }

    @Test
    @DisplayName("guardar: debe retornar 200 con el servicio guardado")
    void guardar_debeRetornar200ConServicioGuardado() {
        when(servicioService.guardar(servicio)).thenReturn(servicio);

        ResponseEntity<Servicio> response = servicioController.guardar(servicio);

        assertEquals(200, response.getStatusCode().value());
        assertSame(servicio, response.getBody());
        verify(servicioService).guardar(servicio);
    }

    @Test
    @DisplayName("listar: debe retornar 200 con la lista de servicios")
    void listar_debeRetornar200ConListaDeServicios() {
        List<Servicio> lista = List.of(servicio);
        when(servicioService.listar()).thenReturn(lista);

        ResponseEntity<List<Servicio>> response = servicioController.listar();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertSame(servicio, response.getBody().get(0));
        verify(servicioService).listar();
    }

    @Test
    @DisplayName("listar: debe retornar lista vacía cuando no hay servicios")
    void listar_debeRetornarListaVaciaCuandoNoHayServicios() {
        when(servicioService.listar()).thenReturn(List.of());

        ResponseEntity<List<Servicio>> response = servicioController.listar();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(servicioService).listar();
    }
}
