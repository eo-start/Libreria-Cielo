/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteIntegrationTest extends BaseIntegrationTest {

    private static Integer clienteIdCreado;

    @BeforeAll
    static void inicializarToken() {
        if (jwtToken == null) {
            jwtToken = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"userName\":\"admin\",\"password\":\"admin123\"}")
                    .when()
                    .post("/auth/login")
                    .then()
                    .extract()
                    .jsonPath()
                    .getString("token");
        }
    }

    @Test
    @Order(1)
    void test01_listarClientes() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/cliente")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(2)
    void test02_crearCliente() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        String body = """
                {
                  "nombreCompleto": "Cliente CI Test",
                  "dni": "99887766",
                  "telefono": "999888777",
                  "correo": "ci@test.com",
                  "direccion": "Jr. Test 123"
                }
                """;

        clienteIdCreado = givenAuth(jwtToken)
                .body(body)
                .when()
                .post("/cliente")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getInt("id");
    }

    @Test
    @Order(3)
    void test03_buscarClientePorId() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/cliente/1")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @Order(4)
    void test04_listarVendedores() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/vendedor")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(5)
    void test05_crearVendedor() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        String body = """
                {
                  "nombre": "Vendedor CI",
                  "dni": "11223344",
                  "telefono": "111222333",
                  "correo": "vendedor@ci.com",
                  "direccion": "Av. CI 456"
                }
                """;

        givenAuth(jwtToken)
                .body(body)
                .when()
                .post("/vendedor")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)));
    }
}
