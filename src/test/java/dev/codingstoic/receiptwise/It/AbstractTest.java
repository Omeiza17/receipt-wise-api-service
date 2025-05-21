package dev.codingstoic.receiptwise.It;

import dev.codingstoic.receiptwise.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@DataJpaTest
@ContextConfiguration(classes = {ApplicantTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
@EnableAutoConfiguration
public abstract class AbstractTest {
    public static final String INIT_SCRIPT = "sql/init.sql";

    public static TestcontainersConfiguration postgres = TestcontainersConfiguration.getInstance(INIT_SCRIPT);

    @BeforeAll
    static void setUp() {
        if (!postgres.isCreated()) {
            postgres.start();
        }
    }

}