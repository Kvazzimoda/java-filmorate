package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmMapper implements RowMapper<Film> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        int filmId = rs.getInt("id");

        Film film = new Film(
                filmId,
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")),
                new LinkedHashSet<>() // сохранить порядок жанров
        );

        String genreSql = """
                    SELECT g.id, g.name
                    FROM genres g
                    JOIN film_genres fg ON g.id = fg.genre_id
                    WHERE fg.film_id = ?
                    ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(genreSql,
                (rs2, rowNum2) -> new Genre(rs2.getInt("id"), rs2.getString("name")),
                filmId);

        film.setGenres(new LinkedHashSet<>(genres));

        return film;
    }
}


