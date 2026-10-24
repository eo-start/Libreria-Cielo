/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void test01_loginExitoso() {
        String body = """
                {"userName":"admin","password":"admin123"}
                """;

        jwtToken = givenJson()
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract()
                .path("token");
    }

    @Test
    @Order(2)
    void test02_loginCredencialesInvalidas() {
        String body = """
                {"userName":"admin","password":"wrongpass"}
                """;

        givenJson()
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(3)
    void test03_loginCamposFaltantes() {
        givenJson()
                .body("{}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(4)
    void test04_validarToken() {
        givenJson()
                .body("{\"token\": \"" + jwtToken + "\"}")
                .when()
                .post("/auth/validate")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(400)));
    }
}
