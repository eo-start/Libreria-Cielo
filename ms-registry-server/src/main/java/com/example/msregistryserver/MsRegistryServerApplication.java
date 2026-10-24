/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msregistryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer  // Habilita Eureka como servidor
public class MsRegistryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsRegistryServerApplication.class, args);  // Ejecuta el servidor de Eureka
	}
}
