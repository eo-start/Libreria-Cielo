/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msventa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI msVentaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-VENTA API - Sistema de Venta de Librería")
                        .version("1.0.0")
                        .description("""
                                API REST profesional para la gestión del carrito de compras,
                                registro de ventas, consulta de ventas y generación de comprobantes PDF.

                                Este microservicio forma parte del Sistema de Venta de Librería y se comunica
                                con ms-auth para validar usuarios mediante JWT y con ms-book para consultar
                                información de libros asociados a las ventas.
                                """)
                        .contact(new Contact()
                                .name("Equipo de Ingeniería de Software")
                                .email("soporte@sistema-libreria.com")
                                .url("https://github.com/eo-start/Libreria-Cielo"))
                        .license(new License()
                                .name("Uso académico - Ingeniería de Software")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingrese el token JWT en el formato: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
