/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.controller;

import com.example.msauth.entity.AuthUser;
import com.example.msauth.service.VinculacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VinculacionControllerTest {

    @Mock
    private VinculacionService vinculacionService;

    @InjectMocks
    private VinculacionController controller;

    private AuthUser cliente;
    private AuthUser vendedor;

    @BeforeEach
    void setUp() {
        cliente = AuthUser.builder()
                .id(1)
                .userName("cliente01")
                .password("encoded-password")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(10)
                .vendedorId(null)
                .build();

        vendedor = AuthUser.builder()
                .id(2)
                .userName("vendedor01")
                .password("encoded-password")
                .rol(AuthUser.Roles.VENDEDOR)
                .clienteId(null)
                .vendedorId(20)
                .build();
    }

    @Test
    void vincularClienteDebeRetornarOkCuandoVinculacionEsCorrecta() {
        when(vinculacionService.vincularCliente(1, 10)).thenReturn(cliente);

        ResponseEntity<AuthUser> response = controller.vincularCliente(1, 10);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("cliente01", response.getBody().getUserName());
        assertEquals(AuthUser.Roles.CLIENTE, response.getBody().getRol());

        verify(vinculacionService).vincularCliente(1, 10);
    }

    @Test
    void vincularClienteDebeRetornarBadRequestCuandoNoSeVincula() {
        when(vinculacionService.vincularCliente(1, 99)).thenReturn(null);

        ResponseEntity<AuthUser> response = controller.vincularCliente(1, 99);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(vinculacionService).vincularCliente(1, 99);
    }

    @Test
    void vincularVendedorDebeRetornarOkCuandoVinculacionEsCorrecta() {
        when(vinculacionService.vincularVendedor(2, 20)).thenReturn(vendedor);

        ResponseEntity<AuthUser> response = controller.vincularVendedor(2, 20);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("vendedor01", response.getBody().getUserName());
        assertEquals(AuthUser.Roles.VENDEDOR, response.getBody().getRol());

        verify(vinculacionService).vincularVendedor(2, 20);
    }

    @Test
    void vincularVendedorDebeRetornarBadRequestCuandoNoSeVincula() {
        when(vinculacionService.vincularVendedor(2, 99)).thenReturn(null);

        ResponseEntity<AuthUser> response = controller.vincularVendedor(2, 99);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(vinculacionService).vincularVendedor(2, 99);
    }
}