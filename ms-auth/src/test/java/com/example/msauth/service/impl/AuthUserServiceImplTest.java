/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.service.impl;

import com.example.msauth.dto.AuthUserDto;
import com.example.msauth.entity.AuthUser;
import com.example.msauth.entity.TokenDto;
import com.example.msauth.repository.AuthUserRepository;
import com.example.msauth.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserServiceImplTest {

    @Mock
    private AuthUserRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthUserServiceImpl authUserService;

    private AuthUser user;
    private AuthUserDto userDto;

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

        userDto = AuthUserDto.builder()
                .userName("admin")
                .password("123456")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(10)
                .vendedorId(null)
                .build();
    }

    @Test
    void saveDebeCrearUsuarioCuandoNoExiste() {
        when(authRepository.findByUserName("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(authRepository.save(any(AuthUser.class))).thenReturn(user);

        AuthUser result = authUserService.save(userDto);

        assertNotNull(result);
        assertEquals("admin", result.getUserName());
        assertEquals(AuthUser.Roles.CLIENTE, result.getRol());
        assertEquals(10, result.getClienteId());

        verify(authRepository).findByUserName("admin");
        verify(passwordEncoder).encode("123456");
        verify(authRepository).save(any(AuthUser.class));
    }

    @Test
    void saveDebeAsignarRolClientePorDefectoCuandoRolEsNull() {
        AuthUserDto dtoSinRol = AuthUserDto.builder()
                .userName("cliente01")
                .password("123456")
                .rol(null)
                .clienteId(20)
                .vendedorId(null)
                .build();

        AuthUser usuarioGuardado = AuthUser.builder()
                .id(2)
                .userName("cliente01")
                .password("encoded-password")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(20)
                .vendedorId(null)
                .build();

        when(authRepository.findByUserName("cliente01")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(authRepository.save(any(AuthUser.class))).thenReturn(usuarioGuardado);

        AuthUser result = authUserService.save(dtoSinRol);

        assertNotNull(result);
        assertEquals(AuthUser.Roles.CLIENTE, result.getRol());
        assertEquals("cliente01", result.getUserName());

        verify(authRepository).save(any(AuthUser.class));
    }

    @Test
    void saveDebeRetornarNullCuandoUsuarioYaExiste() {
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));

        AuthUser result = authUserService.save(userDto);

        assertNull(result);

        verify(authRepository).findByUserName("admin");
        verify(passwordEncoder, never()).encode(anyString());
        verify(authRepository, never()).save(any(AuthUser.class));
    }

    @Test
    void loginDebeRetornarTokenCuandoCredencialesSonCorrectas() {
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtProvider.createToken(user)).thenReturn("jwt-token");

        TokenDto result = authUserService.login(userDto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(authRepository).findByUserName("admin");
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(jwtProvider).createToken(user);
    }

    @Test
    void loginDebeRetornarNullCuandoUsuarioNoExiste() {
        when(authRepository.findByUserName("admin")).thenReturn(Optional.empty());

        TokenDto result = authUserService.login(userDto);

        assertNull(result);

        verify(authRepository).findByUserName("admin");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtProvider, never()).createToken(any(AuthUser.class));
    }

    @Test
    void loginDebeRetornarNullCuandoPasswordEsIncorrecto() {
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(false);

        TokenDto result = authUserService.login(userDto);

        assertNull(result);

        verify(authRepository).findByUserName("admin");
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(jwtProvider, never()).createToken(any(AuthUser.class));
    }

    @Test
    void validateDebeRetornarTokenCuandoTokenEsValidoYUsuarioExiste() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));

        TokenDto result = authUserService.validate("jwt-token");

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());

        verify(jwtProvider).validate("jwt-token");
        verify(jwtProvider).getUserNameFromToken("jwt-token");
        verify(authRepository).findByUserName("admin");
    }

    @Test
    void validateDebeRetornarNullCuandoTokenEsInvalido() {
        when(jwtProvider.validate("bad-token")).thenReturn(false);

        TokenDto result = authUserService.validate("bad-token");

        assertNull(result);

        verify(jwtProvider).validate("bad-token");
        verify(jwtProvider, never()).getUserNameFromToken(anyString());
        verify(authRepository, never()).findByUserName(anyString());
    }

    @Test
    void validateDebeRetornarNullCuandoUsuarioDelTokenNoExiste() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.empty());

        TokenDto result = authUserService.validate("jwt-token");

        assertNull(result);

        verify(jwtProvider).validate("jwt-token");
        verify(jwtProvider).getUserNameFromToken("jwt-token");
        verify(authRepository).findByUserName("admin");
    }

    @Test
    void getUserIdFromTokenDebeRetornarIdCuandoTokenEsValido() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));

        Integer result = authUserService.getUserIdFromToken("jwt-token");

        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    void getUserIdFromTokenDebeRetornarNullCuandoTokenEsInvalido() {
        when(jwtProvider.validate("bad-token")).thenReturn(false);

        Integer result = authUserService.getUserIdFromToken("bad-token");

        assertNull(result);

        verify(jwtProvider).validate("bad-token");
        verify(jwtProvider, never()).getUserNameFromToken(anyString());
    }

    @Test
    void getUserIdFromTokenDebeRetornarNullCuandoUsuarioNoExiste() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.empty());

        Integer result = authUserService.getUserIdFromToken("jwt-token");

        assertNull(result);
    }

    @Test
    void getUserNameFromTokenDebeRetornarUserNameCuandoTokenEsValido() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.of(user));

        String result = authUserService.getUserNameFromToken("jwt-token");

        assertEquals("admin", result);
    }

    @Test
    void getUserNameFromTokenDebeRetornarNullCuandoTokenEsInvalido() {
        when(jwtProvider.validate("bad-token")).thenReturn(false);

        String result = authUserService.getUserNameFromToken("bad-token");

        assertNull(result);

        verify(jwtProvider).validate("bad-token");
        verify(jwtProvider, never()).getUserNameFromToken(anyString());
    }

    @Test
    void getUserNameFromTokenDebeRetornarNullCuandoUsuarioNoExiste() {
        when(jwtProvider.validate("jwt-token")).thenReturn(true);
        when(jwtProvider.getUserNameFromToken("jwt-token")).thenReturn("admin");
        when(authRepository.findByUserName("admin")).thenReturn(Optional.empty());

        String result = authUserService.getUserNameFromToken("jwt-token");

        assertNull(result);
    }

    @Test
    void listarUsuariosDebeRetornarListaUsuarios() {
        when(authRepository.findAll()).thenReturn(List.of(user));

        List<AuthUser> result = authUserService.listarUsuarios();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUserName());

        verify(authRepository).findAll();
    }

    @Test
    void eliminarUsuarioDebeRetornarTrueCuandoUsuarioExiste() {
        when(authRepository.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(authRepository).deleteById(1);

        boolean result = authUserService.eliminarUsuario(1);

        assertTrue(result);

        verify(authRepository).findById(1);
        verify(authRepository).deleteById(1);
    }

    @Test
    void eliminarUsuarioDebeRetornarFalseCuandoUsuarioNoExiste() {
        when(authRepository.findById(99)).thenReturn(Optional.empty());

        boolean result = authUserService.eliminarUsuario(99);

        assertFalse(result);

        verify(authRepository).findById(99);
        verify(authRepository, never()).deleteById(anyInt());
    }
}