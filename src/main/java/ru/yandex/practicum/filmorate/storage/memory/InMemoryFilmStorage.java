package ru.yandex.practicum.filmorate.storage.memory;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.impl.FilmDbStorage;

import java.util.*;

/**
 * In-memory storage for films.
 * @deprecated Use {@link FilmDbStorage} for production-ready database storage.
 */
@Deprecated(since = "1.0", forRemoval = true)
public abstract class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 0;

    @Override
    public Film addFilm(Film film) {
        film.setId(++idCounter);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            return null; // важно: чтобы сервис мог вернуть 404
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }
}

