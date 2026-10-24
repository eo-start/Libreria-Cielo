/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.controller;

import com.example.mscliente.dto.ClienteRequestDto;
import com.example.mscliente.entity.Cliente;
import com.example.mscliente.service.ClienteService;
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
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private Cliente cliente;
    private ClienteRequestDto requestDto;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombreCompleto("Juan");
        cliente.setDni("12345678");
        cliente.setTelefono("999888777");
        cliente.setDireccion("Juliaca");

        requestDto = new ClienteRequestDto();
        requestDto.setNombreCompleto("Juan");
        requestDto.setDni("12345678");
        requestDto.setTelefono("999888777");
        requestDto.setDireccion("Juliaca");
    }

    @Test
    void listarDebeRetornarListaDeClientes() {
        when(clienteService.listar()).thenReturn(List.of(cliente));

        ResponseEntity<List<Cliente>> response = clienteController.listar();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Juan", response.getBody().get(0).getNombreCompleto());

        verify(clienteService).listar();
    }

    @Test
    void guardarDebeRetornarClienteGuardado() {
        when(clienteService.guardar(any(Cliente.class))).thenReturn(cliente);

        ResponseEntity<Cliente> response = clienteController.guardar(requestDto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Juan", response.getBody().getNombreCompleto());
        assertEquals("12345678", response.getBody().getDni());
        assertEquals("999888777", response.getBody().getTelefono());
        assertEquals("Juliaca", response.getBody().getDireccion());

        verify(clienteService).guardar(any(Cliente.class));
    }

    @Test
    void buscarPorIdDebeRetornarClienteCuandoExiste() {
        when(clienteService.buscarPorId(1)).thenReturn(cliente);

        ResponseEntity<Cliente> response = clienteController.buscarPorId(1);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Juan", response.getBody().getNombreCompleto());

        verify(clienteService).buscarPorId(1);
    }

    @Test
    void editarDebeRetornarClienteActualizado() {
        Cliente clienteActualizado = new Cliente();
        clienteActualizado.setId(1);
        clienteActualizado.setNombreCompleto("Juan Actualizado");
        clienteActualizado.setDni("12345678");
        clienteActualizado.setTelefono("988776655");
        clienteActualizado.setDireccion("Puno");

        ClienteRequestDto requestActualizado = new ClienteRequestDto();
        requestActualizado.setNombreCompleto("Juan Actualizado");
        requestActualizado.setDni("12345678");
        requestActualizado.setTelefono("988776655");
        requestActualizado.setDireccion("Puno");

        when(clienteService.editar(any(Cliente.class))).thenReturn(clienteActualizado);

        ResponseEntity<Cliente> response = clienteController.editar(1, requestActualizado);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals("Juan Actualizado", response.getBody().getNombreCompleto());
        assertEquals("988776655", response.getBody().getTelefono());
        assertEquals("Puno", response.getBody().getDireccion());

        verify(clienteService).editar(any(Cliente.class));
    }

    @Test
    void eliminarDebeRetornarMensajeCorrecto() {
        doNothing().when(clienteService).eliminar(1);

        ResponseEntity<String> response = clienteController.eliminar(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Eliminación completa", response.getBody());

        verify(clienteService).eliminar(1);
    }
}
