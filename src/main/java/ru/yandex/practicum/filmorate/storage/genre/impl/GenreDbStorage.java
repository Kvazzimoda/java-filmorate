package ru.yandex.practicum.filmorate.storage.genre.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreMapper genreMapper;

    @Override
    public Collection<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, genreMapper, id);
        return genres.isEmpty() ? Optional.empty() : Optional.of(genres.get(0));
    }

    @Override
    public Collection<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                filmId);
    }
}

