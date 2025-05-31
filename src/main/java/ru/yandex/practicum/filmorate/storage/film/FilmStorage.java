package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Optional<Film> getFilmById(Integer id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    Collection<Film> getPopularFilms(int count);
}