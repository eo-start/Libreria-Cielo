/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.controller;

import com.example.msbook.dto.ProvedoresRequestDto;
import com.example.msbook.entity.Provedores;
import com.example.msbook.service.ProvedoresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvedoresControllerTest {

    @Mock
    private ProvedoresService provedoresService;

    @InjectMocks
    private ProvedoresController provedoresController;

    private Provedores proveedor;
    private ProvedoresRequestDto requestDto;

    @BeforeEach
    void setUp() {
        proveedor = new Provedores();
        proveedor.setId(1);
        proveedor.setNombre("Distribuidora Escolar San José");

        requestDto = new ProvedoresRequestDto();
        requestDto.setNombre("Distribuidora Escolar San José");
    }

    @Test
    void listarProvedoresDebeRetornarLista() {
        when(provedoresService.listar()).thenReturn(List.of(proveedor));

        List<Provedores> response = provedoresController.listarProvedores();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Distribuidora Escolar San José", response.get(0).getNombre());

        verify(provedoresService).listar();
    }

    @Test
    void crearProvedoresDebeRetornarProveedorCreado() {
        when(provedoresService.guardar(any(Provedores.class))).thenReturn(proveedor);

        Provedores response = provedoresController.crearProvedores(requestDto);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Distribuidora Escolar San José", response.getNombre());

        verify(provedoresService).guardar(any(Provedores.class));
    }

    @Test
    void editarProvedoresDebeRetornarProveedorActualizado() {
        Provedores proveedorActualizado = new Provedores();
        proveedorActualizado.setId(1);
        proveedorActualizado.setNombre("Proveedor Actualizado");

        ProvedoresRequestDto requestActualizado = new ProvedoresRequestDto();
        requestActualizado.setNombre("Proveedor Actualizado");

        when(provedoresService.editar(any(Provedores.class), eq(1))).thenReturn(proveedorActualizado);

        Provedores response = provedoresController.editarProvedores(requestActualizado, 1);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals("Proveedor Actualizado", response.getNombre());

        verify(provedoresService).editar(any(Provedores.class), eq(1));
    }

    @Test
    void editarProvedoresDebeRetornarNullCuandoNoExiste() {
        when(provedoresService.editar(any(Provedores.class), eq(99))).thenReturn(null);

        Provedores response = provedoresController.editarProvedores(requestDto, 99);

        assertNull(response);

        verify(provedoresService).editar(any(Provedores.class), eq(99));
    }

    @Test
    void eliminarProvedoresDebeRetornarMensajeCorrecto() {
        doNothing().when(provedoresService).eliminar(1);

        String response = provedoresController.eliminarProvedores(1);

        assertEquals("Se eliminó correctamente", response);

        verify(provedoresService).eliminar(1);
    }
}