package dev.codingstoic.receiptwise.repository;

import dev.codingstoic.receiptwise.It.AbstractTest;
import dev.codingstoic.receiptwise.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

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
        assertEquals("john.doe@example.com", result.get().getEmail());
    }

    @Test
    @Sql(value = {"classpath:sql/clean_script.sql", "classpath:sql/add_user.sql"})
    void itShouldFindByEmail() {
        Optional<User> result = userRepository.findByEmail("jane.smith@example.com");
        assertThat(result).isPresent();
        assertEquals("jane.smith", result.get().getUsername());
    }

}
