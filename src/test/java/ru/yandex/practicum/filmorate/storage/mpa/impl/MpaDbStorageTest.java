package ru.yandex.practicum.filmorate.storage.mpa.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Mpa;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql({"/schema.sql", "/data.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    public void shouldFindMpaById() {
        var mpa = mpaStorage.findById(1);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(1);
                    assertThat(m.getName()).isEqualTo("G");
                });
    }

    @Test
    public void shouldReturnAllRatings() {
        var mpaList = mpaStorage.findAll();

        assertThat(mpaList)
                .hasSize(5)
                .extracting(Mpa::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }
}