/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

@SuppressWarnings({"unused", "java:S1144"})
abstract class BaseIntegrationTest {

    protected static final String USER_TEST = "testventa@upeu.edu.pe";
    protected static final String PASSWORD_TEST = "123456";
    protected static String jwtToken;

    @BeforeAll
    static void configurarRestAssured() {
        RestAssured.baseURI = System.getProperty(
                "external.base-url",
                "http://localhost:8095"
        );
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected RequestSpecification givenJson() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    protected RequestSpecification givenAuth(String token) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    protected static String obtenerTokenJwt() {
        String authBaseUrl = System.getProperty(
                "auth.base-url",
                "http://localhost:8081"
        );

        String requestBody = """
                {
                  "user": "%s",
                  "password": "%s"
                }
                """.formatted(USER_TEST, PASSWORD_TEST);

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(authBaseUrl + "/auth/create")
                .then()
                .extract()
                .statusCode();

        String responseBody = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(authBaseUrl + "/auth/login")
                .then()
                .extract()
                .asString();

        return JsonPath.from(responseBody).getString("token");
    }
}