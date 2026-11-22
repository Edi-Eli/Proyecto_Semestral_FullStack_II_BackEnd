package com.ferreteria.maestros;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifica que el contexto de Spring levanta con todos los beans.
 * Usa el perfil "test" (H2 en memoria) para no depender de PostgreSQL.
 */
@SpringBootTest
@ActiveProfiles("test")
class FerreteriaMaestrosApplicationTests {

	@Test
	void contextLoads() {
	}

}
