/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.service.impl;

import com.example.msbook.entity.Categoria;
import com.example.msbook.repository.CategoriaRepository;
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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1);
        categoria.setNombre("Ciencia");
        categoria.setClasificacion("Académica");
        categoria.setIdioma("Español");
    }

    @Test
    @DisplayName("Debe listar categorías correctamente")
    void listar_debeRetornarListaDeCategorias() {
        when(categoriaRepository.findAll()).thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Ciencia", resultado.get(0).getNombre());
        verify(categoriaRepository).findAll();
    }

    @Test
    @DisplayName("Debe guardar categoría correctamente")
    void guardar_debeRetornarCategoriaGuardada() {
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.guardar(categoria);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Ciencia", resultado.getNombre());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    @DisplayName("Debe editar categoría cuando existe")
    void editar_debeActualizarCategoriaCuandoExiste() {
        when(categoriaRepository.existsById(1)).thenReturn(true);
        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Categoria categoriaNueva = new Categoria();
        categoriaNueva.setNombre("Historia");
        categoriaNueva.setClasificacion("General");
        categoriaNueva.setIdioma("Español");

        Categoria resultado = categoriaService.editar(categoriaNueva, 1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Historia", resultado.getNombre());
        verify(categoriaRepository).existsById(1);
        verify(categoriaRepository).save(categoriaNueva);
    }

    @Test
    @DisplayName("Debe retornar null cuando categoría no existe al editar")
    void editar_debeRetornarNullCuandoNoExiste() {
        when(categoriaRepository.existsById(99)).thenReturn(false);

        Categoria categoriaNueva = new Categoria();
        categoriaNueva.setNombre("Historia");

        Categoria resultado = categoriaService.editar(categoriaNueva, 99);

        assertNull(resultado);
        verify(categoriaRepository).existsById(99);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe eliminar categoría correctamente")
    void eliminar_debeInvocarDeleteById() {
        doNothing().when(categoriaRepository).deleteById(1);

        categoriaService.eliminar(1);

        verify(categoriaRepository).deleteById(1);
    }

    @Test
    @DisplayName("Debe listar categoría por id")
    void listarPorId_debeRetornarOptional() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        Optional<Categoria> resultado = categoriaService.listarPorId(1);

        assertTrue(resultado.isPresent());
        assertEquals("Ciencia", resultado.get().getNombre());
        verify(categoriaRepository).findById(1);
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando no existe categoría")
    void listarPorId_debeRetornarOptionalVacio() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Categoria> resultado = categoriaService.listarPorId(99);

        assertTrue(resultado.isEmpty());
        verify(categoriaRepository).findById(99);
    }
}