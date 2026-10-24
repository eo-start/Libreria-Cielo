/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service.impl;

import com.example.msbook.entity.Provedores;
import com.example.msbook.repository.ProvedoresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProvedoresServiceImplTest {

    @Mock
    private ProvedoresRepository provedoresRepository;

    @InjectMocks
    private ProvedoresServiceImpl provedoresService;

    private Provedores provedores;

    @BeforeEach
    void setUp() {
        provedores = new Provedores();
        provedores.setId(1);
        provedores.setNombre("Proveedor A");
        provedores.setEntrega(new Date());
        provedores.setTipo("Mayorista");
    }

    @Test
    @DisplayName("Debe listar proveedores correctamente")
    void listar_debeRetornarListaDeProveedores() {
        when(provedoresRepository.findAll()).thenReturn(List.of(provedores));

        List<Provedores> resultado = provedoresService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Proveedor A", resultado.get(0).getNombre());
        verify(provedoresRepository).findAll();
    }

    @Test
    @DisplayName("Debe guardar proveedor correctamente")
    void guardar_debeRetornarProveedorGuardado() {
        when(provedoresRepository.save(provedores)).thenReturn(provedores);

        Provedores resultado = provedoresService.guardar(provedores);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Proveedor A", resultado.getNombre());
        verify(provedoresRepository).save(provedores);
    }

    @Test
    @DisplayName("Debe editar proveedor cuando existe")
    void editar_debeActualizarProveedorCuandoExiste() {
        when(provedoresRepository.existsById(1)).thenReturn(true);
        when(provedoresRepository.save(any(Provedores.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Provedores proveedorNuevo = new Provedores();
        proveedorNuevo.setNombre("Proveedor Editado");
        proveedorNuevo.setEntrega(new Date());
        proveedorNuevo.setTipo("Distribuidor");

        Provedores resultado = provedoresService.editar(proveedorNuevo, 1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Proveedor Editado", resultado.getNombre());
        verify(provedoresRepository).existsById(1);
        verify(provedoresRepository).save(proveedorNuevo);
    }

    @Test
    @DisplayName("Debe retornar null cuando proveedor no existe al editar")
    void editar_debeRetornarNullCuandoNoExiste() {
        when(provedoresRepository.existsById(99)).thenReturn(false);

        Provedores proveedorNuevo = new Provedores();
        proveedorNuevo.setNombre("Proveedor X");

        Provedores resultado = provedoresService.editar(proveedorNuevo, 99);

        assertNull(resultado);
        verify(provedoresRepository).existsById(99);
        verify(provedoresRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar proveedor correctamente")
    void eliminar_debeInvocarDeleteById() {
        doNothing().when(provedoresRepository).deleteById(1);

        provedoresService.eliminar(1);

        verify(provedoresRepository).deleteById(1);
    }

    @Test
    @DisplayName("Debe listar proveedor por id")
    void listarPorId_debeRetornarOptional() {
        when(provedoresRepository.findById(1)).thenReturn(Optional.of(provedores));

        Optional<Provedores> resultado = provedoresService.listarPorId(1);

        assertTrue(resultado.isPresent());
        assertEquals("Proveedor A", resultado.get().getNombre());
        verify(provedoresRepository).findById(1);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no existe proveedor")
    void listarPorId_debeRetornarOptionalVacio() {
        when(provedoresRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Provedores> resultado = provedoresService.listarPorId(99);

        assertFalse(resultado.isPresent());
        verify(provedoresRepository).findById(99);
    }
}