package ru.yandex.practicum.filmorate.storage.film.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;
    private final GenreMapper genreMapper;

    @Override
    public Film addFilm(Film film) {
        validateMpa(film.getMpa().getId());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve film ID"
            );
        }
        film.setId(key.intValue());

        insertGenres(film);

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Film ID is required");
        }
        validateMpa(film.getMpa().getId());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        insertGenres(film); // заменил дублирование

        return film;
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmMapper, id);
        if (films.isEmpty()) {
            return Optional.empty();
        }
        Film film = films.getFirst();
        // Используем LinkedHashSet для сохранения порядка
        film.setGenres(new LinkedHashSet<>(getGenresForFilm(id)));
        return Optional.of(film);
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name FROM films f JOIN mpa m ON f.mpa_id = m.id";
        Collection<Film> films = jdbcTemplate.query(sql, filmMapper);
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(getGenresForFilm(film.getId())));
        }
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        if (count != null && count == 0) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, filmId, userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.id AS mpa_id, m.name AS mpa_name, COUNT(l.user_id) AS like_count " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id ORDER BY like_count DESC LIMIT ?";
        Collection<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(getGenresForFilm(film.getId())));
        }
        return films;
    }

    private List<Genre> getGenresForFilm(int filmId) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? ORDER BY g.id";  // Сортировка по id жанра
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId);
    }


    private void validateMpa(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "MPA not found");
        }
    }

    private void validateGenre(int genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found");
        }
    }

    // доработал работу метода
    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            // Удаляем все жанры, если фильм теперь без жанров
            jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
            return;
        }

        // Удаляем старые связи перед вставкой новых
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        film.getGenres().stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .forEach(genre -> {
                    validateGenre(genre.getId()); // проверка существования жанра
                    jdbcTemplate.update(sql, film.getId(), genre.getId());
                });
    }
}
