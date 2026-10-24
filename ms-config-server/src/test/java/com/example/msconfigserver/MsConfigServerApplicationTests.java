/* Libreria Cielo | Uso exclusivo autorizado para este proyecto. Otros usos prohibidos salvo derechos o licencias previos. Ver USO_RESTRINGIDO.md. */
package com.example.msconfigserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "CONFIG_SERVER_PASSWORD=config-server-test-only")
class MsConfigServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
