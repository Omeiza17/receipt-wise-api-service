package dev.codingstoic.receiptwise.repository;

import dev.codingstoic.receiptwise.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@ExtendWith(SpringExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("johnDoe");
        user.setEmail("john@example.com");
        user.setPassword("password");
        userRepository.save(user);
    }

    @Test
    void itShouldFindByUsername() {
        Optional<User> result = userRepository.findByUsername("johnDoe");
        assertThat(result).isPresent();
        assertEquals("john@example.com", result.get().getEmail());
    }


}
