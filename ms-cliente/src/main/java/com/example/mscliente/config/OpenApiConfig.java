/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.mscliente.config;

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
    public OpenAPI msClienteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-CLIENTE API - Sistema de Venta de Librería")
                        .version("1.0.0")
                        .description("""
                                API REST profesional para la gestión de clientes y vendedores
                                del Sistema de Venta de Librería. Este microservicio permite registrar,
                                listar, buscar, actualizar y eliminar información de clientes y vendedores.

                                Forma parte de una arquitectura basada en microservicios con Spring Boot,
                                Spring Cloud, Eureka Client, Config Server y API Gateway.
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
