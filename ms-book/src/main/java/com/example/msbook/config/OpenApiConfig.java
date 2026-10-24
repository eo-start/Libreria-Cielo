/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI msBookOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-BOOK API - Sistema de Venta de Librería")
                        .version("1.0.0")
                        .description("""
                                API REST profesional para la gestión del catálogo de libros,
                                categorías, proveedores, stock, filtros por categoría, filtros por año
                                y acceso a imágenes asociadas a los libros.

                                Este microservicio forma parte del Sistema de Venta de Librería
                                desarrollado bajo una arquitectura de microservicios con Spring Boot,
                                Spring Cloud, Eureka Client y Config Server.
                                """)
                        .contact(new Contact()
                                .name("Equipo de Ingeniería de Software")
                                .email("soporte@sistema-libreria.com")
                                .url("https://github.com/eo-start/Libreria-Cielo"))
                        .license(new License()
                                .name("Uso académico - Ingeniería de Software")));
    }
}
