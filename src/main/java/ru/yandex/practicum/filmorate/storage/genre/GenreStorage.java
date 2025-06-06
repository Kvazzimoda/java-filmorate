package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> findAll();

    Optional<Genre> findById(int id);

    Collection<Genre> getGenresByFilmId(int filmId); // уже есть
}