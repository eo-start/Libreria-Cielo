/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.controller;

import com.example.mscliente.dto.VendedorRequestDto;
import com.example.mscliente.entity.Vendedor;
import com.example.mscliente.service.VendedorService;
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
class VendedorControllerTest {

    @Mock
    private VendedorService vendedorService;

    @InjectMocks
    private VendedorController vendedorController;

    @Test
    void listarVendedorDebeRetornarListaDeVendedores() {
        Vendedor vendedor = crearVendedor();

        when(vendedorService.listar()).thenReturn(List.of(vendedor));

        ResponseEntity<List<Vendedor>> response = vendedorController.listarVendedor();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("María Quispe", response.getBody().get(0).getNombre());

        verify(vendedorService, times(1)).listar();
    }

    @Test
    void crearVendedorDebeRetornarVendedorCreado() {
        VendedorRequestDto request = crearVendedorRequestDto();
        Vendedor vendedorGuardado = crearVendedor();

        when(vendedorService.guardar(any(Vendedor.class))).thenReturn(vendedorGuardado);

        ResponseEntity<Vendedor> response = vendedorController.crearVendedor(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("María Quispe", response.getBody().getNombre());
        assertEquals("71234567", response.getBody().getDni());

        verify(vendedorService, times(1)).guardar(any(Vendedor.class));
    }

    @Test
    void eliminarVendedorDebeRetornarMensajeCorrecto() {
        doNothing().when(vendedorService).eliminar(1);

        ResponseEntity<String> response = vendedorController.eliminarVendedor(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Se eliminó correctamente", response.getBody());

        verify(vendedorService, times(1)).eliminar(1);
    }

    @Test
    void editarVendedorDebeRetornarVendedorActualizado() {
        VendedorRequestDto request = crearVendedorRequestDto();
        Vendedor vendedorActualizado = crearVendedor();
        vendedorActualizado.setNombre("María Actualizada");

        when(vendedorService.editar(any(Vendedor.class), eq(1))).thenReturn(vendedorActualizado);

        ResponseEntity<Vendedor> response = vendedorController.editarVendedor(request, 1);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("María Actualizada", response.getBody().getNombre());

        verify(vendedorService, times(1)).editar(any(Vendedor.class), eq(1));
    }

    private Vendedor crearVendedor() {
        Vendedor vendedor = new Vendedor();
        vendedor.setId(1);
        vendedor.setNombre("María Quispe");
        vendedor.setDni("71234567");
        vendedor.setTelefono("987123456");
        vendedor.setCorreo("vendedor@libreria.com");
        vendedor.setDireccion("Av. Circunvalación 456");
        return vendedor;
    }

    private VendedorRequestDto crearVendedorRequestDto() {
        VendedorRequestDto request = new VendedorRequestDto();
        request.setNombre("María Quispe");
        request.setDni("71234567");
        request.setTelefono("987123456");
        request.setCorreo("vendedor@libreria.com");
        request.setDireccion("Av. Circunvalación 456");
        return request;
    }
}