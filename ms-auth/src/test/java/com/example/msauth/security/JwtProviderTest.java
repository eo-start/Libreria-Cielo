/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth.security;

import com.example.msauth.entity.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();

        ReflectionTestUtils.setField(
                jwtProvider,
                "secret",
                "test-secret-key-for-jwt-provider-testing-123456789"
        );

        jwtProvider.init();

        authUser = AuthUser.builder()
                .id(1)
                .userName("admin")
                .password("encoded-password")
                .rol(AuthUser.Roles.CLIENTE)
                .clienteId(10)
                .vendedorId(null)
                .build();
    }

    @Test
    void createTokenDebeGenerarTokenValido() {
        String token = jwtProvider.createToken(authUser);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtProvider.validate(token));
    }

    @Test
    void validateDebeRetornarTrueCuandoTokenEsValido() {
        String token = jwtProvider.createToken(authUser);

        boolean result = jwtProvider.validate(token);

        assertTrue(result);
    }

    @Test
    void validateDebeRetornarFalseCuandoTokenEsInvalido() {
        boolean result = jwtProvider.validate("token-invalido");

        assertFalse(result);
    }

    @Test
    void getUserNameFromTokenDebeRetornarNombreUsuarioCuandoTokenEsValido() {
        String token = jwtProvider.createToken(authUser);

        String result = jwtProvider.getUserNameFromToken(token);

        assertEquals("admin", result);
    }

    @Test
    void getUserNameFromTokenDebeRetornarBadTokenCuandoTokenEsInvalido() {
        String result = jwtProvider.getUserNameFromToken("token-invalido");

        assertEquals("bad token", result);
    }
}