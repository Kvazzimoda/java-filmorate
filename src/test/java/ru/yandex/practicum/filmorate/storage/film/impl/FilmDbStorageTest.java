package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql({"/schema.sql", "/data.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM films");
    }

    @Test
    public void shouldAddAndFindFilmById() {
        Film film = createTestFilm();

        filmStorage.addFilm(film);
        Optional<Film> result = filmStorage.getFilmById(film.getId());

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(film.getId());
                    assertThat(f.getName()).isEqualTo("Matrix");
                    assertThat(f.getMpa().getId()).isEqualTo(1);
                    assertThat(f.getGenres()).hasSize(1)
                            .extracting(Genre::getId)
                            .containsExactly(1);
                });
    }

    @Test
    public void shouldUpdateFilm() {
        Film film = createTestFilm();
        filmStorage.addFilm(film);

        film.setName("New Name");
        film.setDescription("Updated description");
        film.setMpa(new Mpa(3, "PG-13"));
        film.setGenres(Set.of(new Genre(2, null), new Genre(3, null)));

        filmStorage.updateFilm(film);
        Optional<Film> updated = filmStorage.getFilmById(film.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("New Name");
                    assertThat(f.getDescription()).isEqualTo("Updated description");
                    assertThat(f.getMpa().getId()).isEqualTo(3);
                    assertThat(f.getGenres())
                            .extracting(Genre::getId)
                            .containsExactlyInAnyOrder(2, 3);
                });
    }

    @Test
    public void shouldReturnAllFilms() {
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Inception");

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        List<Film> films = List.copyOf(filmStorage.getAllFilms());

        assertThat(films)
                .hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Matrix", "Inception");
    }

    private Film createTestFilm() {
        return new Film(
                null,
                "Matrix",
                "Neo discovers reality",
                LocalDate.of(1999, 3, 31),
                136,
                new Mpa(1, null),
                Set.of(new Genre(1, null))
        );
    }
}