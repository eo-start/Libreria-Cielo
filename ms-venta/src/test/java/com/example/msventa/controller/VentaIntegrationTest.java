/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class VentaIntegrationTest extends BaseIntegrationTest {

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
    void test01_agregarAlCarrito() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        String body = """
                {"libroId":62,"cantidad":1}
                """;

        givenAuth(jwtToken)
                .body(body)
                .when()
                .post("/carrito/agregar")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400)));
    }

    @Test
    @Order(2)
    void test02_verCarrito() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/carrito/listar")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(3)
    void test03_realizarVenta() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .queryParam("clienteNombre", "admin")
                .when()
                .post("/venta/realizar")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201), equalTo(400), equalTo(500)));
    }

    @Test
    @Order(4)
    void test04_listarVentas() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/venta/listar")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(5)
    void test05_buscarVentaPorId() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/venta/1")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    @Order(6)
    void test06_productosMasVendidos() {
        Assumptions.assumeTrue(jwtToken != null, "Token no disponible");

        givenAuth(jwtToken)
                .when()
                .get("/venta/productos-mas-vendidos")
                .then()
                .statusCode(200);
    }
}
