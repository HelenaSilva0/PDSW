package vivamama;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:vivamama-test;DB_CLOSE_DELAY=-1",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.h2.console.enabled=false",
		"spring.jpa.show-sql=false",
		"security.jwt.secret=test-only-secret-that-is-long-enough-for-hmac"
})
class VivaMamaApplicationTests {

	@Test
	void contextLoads() {
	}

}
