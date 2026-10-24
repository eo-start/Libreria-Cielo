/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msgatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsGatewayServiceApplication {

    /**
     * Método principal que arranca la aplicación del microservicio Gateway.
     * Se utiliza SpringApplication.run() para iniciar el contexto de la aplicación.
     * Este microservicio actúa como un punto central para enrutar las solicitudes
     * a los demás microservicios.
     *
     * @param args Parámetros de línea de comandos, si los hay.
     */
    public static void main(String[] args) {
        // Inicia la aplicación Spring Boot para el microservicio Gateway.
        SpringApplication.run(MsGatewayServiceApplication.class, args);
    }
}
