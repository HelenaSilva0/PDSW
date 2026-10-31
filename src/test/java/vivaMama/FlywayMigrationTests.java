package vivaMama;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import vivamama.VivaMamaApplication;

/** Aplica as migrations e exige que o schema resultante seja compatível com as entidades JPA. */
@SpringBootTest(classes = VivaMamaApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:vivamama-flyway;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.h2.console.enabled=false",
        "spring.jpa.show-sql=false",
        "security.jwt.secret=test-only-secret-that-is-long-enough-for-hmac"
})
class FlywayMigrationTests {
    @Test
    void migrationsMatchEntities() { }
}
