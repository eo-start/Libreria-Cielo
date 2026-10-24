/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibroIntegrationTest extends BaseIntegrationTest {

    private static Integer categoriaIdCreado;
    private static Integer proveedorIdCreado;

    @Test
    @Order(1)
    void test01_listarLibros() {
        givenJson()
                .when()
                .get("/libros")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(2)
    void test02_buscarLibroPorIdExistente() {
        givenJson()
                .when()
                .get("/libros/62")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @Order(3)
    void test03_buscarLibroPorIdInexistente() {
        givenJson()
                .when()
                .get("/libros/99999")
                .then()
                .statusCode(anyOf(equalTo(404), equalTo(400)));
    }

    @Test
    @Order(4)
    void test04_listarCategorias() {
        givenJson()
                .when()
                .get("/categoria")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(5)
    void test05_crearCategoria() {
        String body = """
                {
                  "nombre": "Test CI",
                  "clasificacion": "Test",
                  "formato": "IMPRESO",
                  "idioma": "ES"
                }
                """;

        categoriaIdCreado = givenJson()
                .body(body)
                .when()
                .post("/categoria")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getInt("id");
    }

    @Test
    @Order(6)
    void test06_buscarCategoriaPorId() {
        Assumptions.assumeTrue(categoriaIdCreado != null, "Requiere que test05 haya creado la categoría");

        givenJson()
                .when()
                .get("/categoria/" + categoriaIdCreado)
                .then()
                .statusCode(200)
                .body("id", equalTo(categoriaIdCreado));
    }

    @Test
    @Order(7)
    void test07_editarCategoria() {
        Assumptions.assumeTrue(categoriaIdCreado != null, "Requiere que test05 haya creado la categoría");

        String body = """
                {
                  "nombre": "Test CI Editado",
                  "clasificacion": "Test",
                  "formato": "IMPRESO",
                  "idioma": "ES"
                }
                """;

        givenJson()
                .body(body)
                .when()
                .put("/categoria/" + categoriaIdCreado)
                .then()
                .statusCode(200);
    }

    @Test
    @Order(8)
    void test08_listarProveedores() {
        givenJson()
                .when()
                .get("/provedores")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    @Order(9)
    void test09_crearProveedor() {
        String body = """
                {
                  "nombre": "Proveedor CI Test",
                  "tipo": "Nacional"
                }
                """;

        proveedorIdCreado = givenJson()
                .body(body)
                .when()
                .post("/provedores")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("id", notNullValue())
                .extract()
                .jsonPath()
                .getInt("id");
    }
}
