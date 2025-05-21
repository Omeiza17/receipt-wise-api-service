package dev.codingstoic.receiptwise.repository;

import dev.codingstoic.receiptwise.It.AbstractTest;
import dev.codingstoic.receiptwise.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class UserRepositoryTest extends AbstractTest {

    @Autowired
    private UserRepository userRepository;



    @Test
    @Sql(value = {"classpath:sql/clean_script.sql", "classpath:sql/add_user.sql"})
    void itShouldFindByUsername() {
        Optional<User> result = userRepository.findByUsername("john.doe");
        assertThat(result).isPresent();
        assertEquals("john@example.com", result.get().getEmail());
    }


}
