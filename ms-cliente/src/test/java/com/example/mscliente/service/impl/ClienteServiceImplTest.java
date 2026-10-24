/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.service.impl;

import com.example.mscliente.entity.Cliente;
import com.example.mscliente.repository.ClienteRepository;
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
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombreCompleto("Juan");
        cliente.setDni("12345678");
        cliente.setTelefono("999888777");
        cliente.setDireccion("Juliaca");
    }

    @Test
    @DisplayName("Debe listar clientes correctamente")
    void listar_debeRetornarListaDeClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        List<Cliente> resultado = clienteService.listar();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombreCompleto());
        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Debe guardar cliente correctamente")
    void guardar_debeRetornarClienteGuardado() {
        // guardar() llama count() para generar el código antes de save()
        when(clienteRepository.count()).thenReturn(0L);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.guardar(cliente);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Juan", resultado.getNombreCompleto());
        verify(clienteRepository).count();
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debe buscar cliente por id cuando existe")
    void buscarPorId_debeRetornarClienteCuandoExiste() {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorId(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Juan", resultado.getNombreCompleto());
        verify(clienteRepository).findById(1);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cliente no existe")
    void buscarPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(clienteRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.buscarPorId(99)
        );

        assertEquals("Cliente no encontrado", ex.getMessage());
        verify(clienteRepository).findById(99);
    }

    @Test
    @DisplayName("Debe editar cliente correctamente")
    void editar_debeRetornarClienteEditado() {
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.editar(cliente);

        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("Juan", resultado.getNombreCompleto());
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Debe eliminar cliente correctamente")
    void eliminar_debeInvocarDeleteById() {
        doNothing().when(clienteRepository).deleteById(1);

        clienteService.eliminar(1);

        verify(clienteRepository).deleteById(1);
    }
}
