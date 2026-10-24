/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.service.impl;

import com.example.mscliente.entity.Vendedor;
import com.example.mscliente.repository.VendedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendedorServiceImplTest {

    @Mock
    private VendedorRepository vendedorRepository;

    @InjectMocks
    private VendedorServiceImpl vendedorService;

    private Vendedor vendedor;

    @BeforeEach
    void setUp() {
        vendedor = new Vendedor();
        vendedor.setId(1);
        vendedor.setNombre("Carlos");
        vendedor.setDni("87654321");
        vendedor.setTelefono("987654321");
        vendedor.setCorreo("carlos@gmail.com");
        vendedor.setDireccion("Juliaca");
    }

    @Test
    @DisplayName("Debe listar vendedores correctamente")
    void listar_debeRetornarListaDeVendedores() {
        when(vendedorRepository.findAll()).thenReturn(List.of(vendedor));

        List<Vendedor> resultado = vendedorService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Carlos", resultado.get(0).getNombre());
        verify(vendedorRepository).findAll();
    }

    @Test
    @DisplayName("Debe guardar vendedor correctamente")
    void guardar_debeRetornarVendedorGuardado() {
        when(vendedorRepository.save(vendedor)).thenReturn(vendedor);

        Vendedor resultado = vendedorService.guardar(vendedor);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Carlos", resultado.getNombre());
        verify(vendedorRepository).save(vendedor);
    }

    @Test
    @DisplayName("Debe editar vendedor cuando existe")
    void editar_debeActualizarVendedorCuandoExiste() {
        when(vendedorRepository.existsById(1)).thenReturn(true);
        when(vendedorRepository.save(any(Vendedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vendedor vendedorNuevo = new Vendedor();
        vendedorNuevo.setNombre("Carlos Editado");
        vendedorNuevo.setDni("87654321");
        vendedorNuevo.setTelefono("987654321");
        vendedorNuevo.setCorreo("carloseditado@gmail.com");
        vendedorNuevo.setDireccion("Puno");

        Vendedor resultado = vendedorService.editar(vendedorNuevo, 1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Carlos Editado", resultado.getNombre());
        verify(vendedorRepository).existsById(1);
        verify(vendedorRepository).save(vendedorNuevo);
    }

    @Test
    @DisplayName("Debe retornar null cuando vendedor no existe al editar")
    void editar_debeRetornarNullCuandoNoExiste() {
        when(vendedorRepository.existsById(99)).thenReturn(false);

        Vendedor vendedorNuevo = new Vendedor();
        vendedorNuevo.setNombre("No Existe");

        Vendedor resultado = vendedorService.editar(vendedorNuevo, 99);

        assertNull(resultado);
        verify(vendedorRepository).existsById(99);
        verify(vendedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar vendedor correctamente")
    void eliminar_debeInvocarDeleteById() {
        doNothing().when(vendedorRepository).deleteById(1);

        vendedorService.eliminar(1);

        verify(vendedorRepository).deleteById(1);
    }
}