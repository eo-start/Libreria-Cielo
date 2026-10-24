/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.service;

import com.example.msventa.entity.Servicio;
import com.example.msventa.repository.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioServiceImplTest {

    private static final Date FECHA_FIJA = new Date(0L);

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioServiceImpl servicioService;

    private Servicio servicio;

    @BeforeEach
    void setUp() {
        servicio = new Servicio();
        servicio.setTipoServicio("FOTOCOPIADO");
        servicio.setCliente("Cliente Test");
        servicio.setCantidad(5);
        servicio.setPrecioUnitario(0.50);
        servicio.setTotal(2.50);
    }

    @Test
    @DisplayName("guardar: debe asignar fecha actual cuando fecha es null")
    void guardar_debeAsignarFechaCuandoEsNull() {
        servicio.setFecha(null);
        servicio.setMetodoPago("YAPE");
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        Servicio resultado = servicioService.guardar(servicio);

        assertNotNull(resultado.getFecha());
        verify(servicioRepository).save(servicio);
    }

    @Test
    @DisplayName("guardar: no debe sobreescribir fecha cuando ya tiene valor")
    void guardar_noDebeModificarFechaCuandoYaTieneValor() {
        servicio.setFecha(FECHA_FIJA);
        servicio.setMetodoPago("EFECTIVO");
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        servicioService.guardar(servicio);

        assertEquals(FECHA_FIJA, servicio.getFecha());
        verify(servicioRepository).save(servicio);
    }

    @ParameterizedTest(name = "metodoPago=''{0}'' -> esperado=''{1}''")
    @CsvSource(value = {
        "NULL, EFECTIVO",
        "'   ', EFECTIVO",
        "TRANSFERENCIA, TRANSFERENCIA"
    }, nullValues = "NULL")
    @DisplayName("guardar: debe gestionar metodoPago correctamente según su valor")
    void guardar_debeGestionarMetodoPagoCorrectamente(String metodoPago, String esperado) {
        servicio.setFecha(FECHA_FIJA);
        servicio.setMetodoPago(metodoPago);
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        servicioService.guardar(servicio);

        assertEquals(esperado, servicio.getMetodoPago());
        verify(servicioRepository).save(servicio);
    }

    @Test
    @DisplayName("listar: debe delegar al repositorio")
    void listar_debeRetornarListaDelRepositorio() {
        List<Servicio> lista = List.of(servicio);
        when(servicioRepository.findAllByOrderByFechaDesc()).thenReturn(lista);

        List<Servicio> resultado = servicioService.listar();

        assertEquals(1, resultado.size());
        assertSame(servicio, resultado.get(0));
        verify(servicioRepository).findAllByOrderByFechaDesc();
    }
}
