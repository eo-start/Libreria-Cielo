/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.controller;

import com.example.msauth.dto.AuthUserDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.entity.TokenDto;
import com.example.msauth.service.AuthUserService;
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
class AuthUserControllerTest {

    @Mock
    private AuthUserService authUserService;

    @InjectMocks
    private AuthUserController controller;

    private AuthUserDto userDto;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        userDto = AuthUserDto.builder()
                .userName("admin")
                .password("123456")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(10)
                .vendedorId(null)
                .build();

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
    void loginDebeRetornarOkCuandoCredencialesSonCorrectas() {
        TokenDto tokenDto = new TokenDto("jwt-token");

        when(authUserService.login(userDto)).thenReturn(tokenDto);

        ResponseEntity<TokenDto> response = controller.login(userDto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());

        verify(authUserService).login(userDto);
    }

    @Test
    void loginDebeRetornarBadRequestCuandoCredencialesSonInvalidas() {
        when(authUserService.login(userDto)).thenReturn(null);

        ResponseEntity<TokenDto> response = controller.login(userDto);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authUserService).login(userDto);
    }

    @Test
    void validateDebeRetornarOkCuandoTokenEsValido() {
        TokenDto tokenDto = new TokenDto("jwt-token");

        when(authUserService.validate("jwt-token")).thenReturn(tokenDto);

        ResponseEntity<TokenDto> response = controller.validate("jwt-token");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt-token", response.getBody().getToken());

        verify(authUserService).validate("jwt-token");
    }

    @Test
    void validateDebeRetornarBadRequestCuandoTokenEsInvalido() {
        when(authUserService.validate("bad-token")).thenReturn(null);

        ResponseEntity<TokenDto> response = controller.validate("bad-token");

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authUserService).validate("bad-token");
    }

    @Test
    void createDebeRetornarOkCuandoUsuarioSeCreaCorrectamente() {
        when(authUserService.save(userDto)).thenReturn(user);

        ResponseEntity<AuthUser> response = controller.create(userDto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("admin", response.getBody().getUserName());

        verify(authUserService).save(userDto);
    }

    @Test
    void createDebeRetornarBadRequestCuandoUsuarioNoSeCrea() {
        when(authUserService.save(userDto)).thenReturn(null);

        ResponseEntity<AuthUser> response = controller.create(userDto);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authUserService).save(userDto);
    }

    @Test
    void listarUsuariosDebeRetornarOkConListaUsuarios() {
        when(authUserService.listarUsuarios()).thenReturn(List.of(user));

        ResponseEntity<List<AuthUser>> response = controller.listarUsuarios();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("admin", response.getBody().get(0).getUserName());

        verify(authUserService).listarUsuarios();
    }

    @Test
    void eliminarUsuarioDebeRetornarOkCuandoUsuarioExiste() {
        when(authUserService.eliminarUsuario(1)).thenReturn(true);

        ResponseEntity<Void> response = controller.eliminarUsuario(1);

        assertEquals(200, response.getStatusCode().value());

        verify(authUserService).eliminarUsuario(1);
    }

    @Test
    void eliminarUsuarioDebeRetornarNotFoundCuandoUsuarioNoExiste() {
        when(authUserService.eliminarUsuario(99)).thenReturn(false);

        ResponseEntity<Void> response = controller.eliminarUsuario(99);

        assertEquals(404, response.getStatusCode().value());

        verify(authUserService).eliminarUsuario(99);
    }

    @Test
    void getUserIdDebeRetornarOkCuandoTokenEsValidoConBearer() {
        when(authUserService.getUserIdFromToken("jwt-token")).thenReturn(1);

        ResponseEntity<Integer> response = controller.getUserId("Bearer jwt-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody());

        verify(authUserService).getUserIdFromToken("jwt-token");
    }

    @Test
    void getUserIdDebeRetornarOkCuandoTokenVieneSinBearer() {
        when(authUserService.getUserIdFromToken("jwt-token")).thenReturn(1);

        ResponseEntity<Integer> response = controller.getUserId("jwt-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody());

        verify(authUserService).getUserIdFromToken("jwt-token");
    }

    @Test
    void getUserIdDebeRetornarBadRequestCuandoNoEncuentraUsuario() {
        when(authUserService.getUserIdFromToken("bad-token")).thenReturn(null);

        ResponseEntity<Integer> response = controller.getUserId("Bearer bad-token");

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authUserService).getUserIdFromToken("bad-token");
    }

    @Test
    void getUserNameDebeRetornarOkCuandoTokenEsValidoConBearer() {
        when(authUserService.getUserNameFromToken("jwt-token")).thenReturn("admin");

        ResponseEntity<String> response = controller.getUserName("Bearer jwt-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("admin", response.getBody());

        verify(authUserService).getUserNameFromToken("jwt-token");
    }

    @Test
    void getUserNameDebeRetornarOkCuandoTokenVieneSinBearer() {
        when(authUserService.getUserNameFromToken("jwt-token")).thenReturn("admin");

        ResponseEntity<String> response = controller.getUserName("jwt-token");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("admin", response.getBody());

        verify(authUserService).getUserNameFromToken("jwt-token");
    }

    @Test
    void getUserNameDebeRetornarBadRequestCuandoNoEncuentraUsuario() {
        when(authUserService.getUserNameFromToken("bad-token")).thenReturn(null);

        ResponseEntity<String> response = controller.getUserName("Bearer bad-token");

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());

        verify(authUserService).getUserNameFromToken("bad-token");
    }
}