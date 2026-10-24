/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service.impl;

import com.example.msauth.dto.ClienteDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.feign.ClienteFeign;
import com.example.msauth.repository.AuthUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VinculacionServiceImplTest {

    @Mock
    private AuthUserRepository authRepository;

    @Mock
    private ClienteFeign clienteFeign;

    @InjectMocks
    private VinculacionServiceImpl vinculacionService;

    private AuthUser user;

    @BeforeEach
    void setUp() {
        user = AuthUser.builder()
                .id(1)
                .userName("admin")
                .password("encoded-password")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(10)
                .vendedorId(null)
                .build();
    }

    @Test
    void vincularClienteDebeRetornarNullCuandoUsuarioNoExiste() {
        when(authRepository.findById(99)).thenReturn(Optional.empty());

        AuthUser result = vinculacionService.vincularCliente(99, 10);

        assertNull(result);

        verify(authRepository).findById(99);
        verify(clienteFeign, never()).ListbyId(anyInt());
        verify(authRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void vincularClienteDebeRetornarNullCuandoClienteNoExiste() {
        when(authRepository.findById(1)).thenReturn(Optional.of(user));
        when(clienteFeign.ListbyId(99)).thenReturn(ResponseEntity.ok(null));

        AuthUser result = vinculacionService.vincularCliente(1, 99);

        assertNull(result);

        verify(authRepository).findById(1);
        verify(clienteFeign).ListbyId(99);
        verify(authRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void vincularClienteDebeActualizarClienteYRolCliente() {
        ClienteDto clienteDto = new ClienteDto();

        when(authRepository.findById(1)).thenReturn(Optional.of(user));
        when(clienteFeign.ListbyId(20)).thenReturn(ResponseEntity.ok(clienteDto));
        when(authRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthUser result = vinculacionService.vincularCliente(1, 20);

        assertNotNull(result);
        assertEquals(20, result.getClienteId());
        assertNull(result.getVendedorId());
        assertEquals(AuthUser.Roles.CLIENTE, result.getRol());

        verify(authRepository).findById(1);
        verify(clienteFeign).ListbyId(20);
        verify(authRepository).save(user);
    }

    @Test
    void vincularVendedorDebeRetornarNullCuandoUsuarioNoExiste() {
        when(authRepository.findById(99)).thenReturn(Optional.empty());

        AuthUser result = vinculacionService.vincularVendedor(99, 30);

        assertNull(result);

        verify(authRepository).findById(99);
        verify(clienteFeign, never()).ListbyId(anyInt());
        verify(authRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void vincularVendedorDebeRetornarNullCuandoVendedorNoExiste() {
        when(authRepository.findById(1)).thenReturn(Optional.of(user));
        when(clienteFeign.ListbyId(99)).thenReturn(ResponseEntity.ok(null));

        AuthUser result = vinculacionService.vincularVendedor(1, 99);

        assertNull(result);

        verify(authRepository).findById(1);
        verify(clienteFeign).ListbyId(99);
        verify(authRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void vincularVendedorDebeActualizarVendedorYRolVendedor() {
        ClienteDto vendedorDto = new ClienteDto();

        when(authRepository.findById(1)).thenReturn(Optional.of(user));
        when(clienteFeign.ListbyId(30)).thenReturn(ResponseEntity.ok(vendedorDto));
        when(authRepository.save(any(AuthUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthUser result = vinculacionService.vincularVendedor(1, 30);

        assertNotNull(result);
        assertNull(result.getClienteId());
        assertEquals(30, result.getVendedorId());
        assertEquals(AuthUser.Roles.VENDEDOR, result.getRol());

        verify(authRepository).findById(1);
        verify(clienteFeign).ListbyId(30);
        verify(authRepository).save(user);
    }
}