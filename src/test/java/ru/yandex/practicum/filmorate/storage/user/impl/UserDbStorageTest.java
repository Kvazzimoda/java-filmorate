package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql({"/schema.sql", "/data.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friendships");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void shouldAddAndFindUserById() {
        User user = createTestUser();
        userStorage.addUser(user);

        Optional<User> result = userStorage.getUserById(user.getId());

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(user.getId());
                    assertThat(u.getEmail()).isEqualTo("mail@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("login");
                    assertThat(u.getName()).isEqualTo("name");
                });
    }

    private User createTestUser() {
        return new User(
                null,
                "mail@mail.ru",
                "login",
                "name",
                LocalDate.of(1990, 1, 1)
        );
    }
}