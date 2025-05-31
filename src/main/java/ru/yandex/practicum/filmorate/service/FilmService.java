package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;


@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {

        Film createdFilm = filmStorage.addFilm(film);

        // Извлекаем жанры, сортируем по id и устанавливаем обратно
        if (createdFilm.getGenres() != null) {
            List<Genre> sortedGenres = new ArrayList<>(createdFilm.getGenres());
            sortedGenres.sort(Comparator.comparingInt(Genre::getId));
            createdFilm.setGenres(new LinkedHashSet<>(sortedGenres));
        }
        return createdFilm;
    }

    public Film updateFilm(Film film) {
        Film updatedFilm = filmStorage.updateFilm(film);

        // Извлекаем жанры, сортируем по id и устанавливаем обратно
        if (updatedFilm.getGenres() != null) {
            List<Genre> sortedGenres = new ArrayList<>(updatedFilm.getGenres());
            sortedGenres.sort(Comparator.comparingInt(Genre::getId));
            updatedFilm.setGenres(new LinkedHashSet<>(sortedGenres));
        }
        return updatedFilm;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found"));
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

}