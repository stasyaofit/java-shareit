package ru.practicum.shareit.user.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("test")
                .email("test@test.ru")
                .build();
        repository.save(user);
    }

    @Test
    void findById() {
        Optional<User> actual = repository.findById(user.getId());
        actual.ifPresent(value -> assertEquals(value, user));
    }
}