/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		properties = {
				"spring.cloud.config.enabled=false",
				"spring.config.import=",
				"eureka.client.enabled=false",
				"eureka.client.register-with-eureka=false",
				"eureka.client.fetch-registry=false",
				"spring.datasource.url=jdbc:h2:mem:ms_auth_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
				"spring.datasource.driver-class-name=org.h2.Driver",
				"spring.datasource.username=sa",
				"spring.datasource.password=",
				"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
				"spring.jpa.hibernate.ddl-auto=create-drop",
				"spring.jpa.show-sql=false",
				"jwt.secret=test-secret-key-for-testing-purposes-only-123456789",
				"spring.main.allow-bean-definition-overriding=true",
				"spring.main.web-application-type=servlet"
		}
)
@ActiveProfiles("test")
class MsAuthApplicationTests {

	@Test
	void contextLoads() {
	}
}