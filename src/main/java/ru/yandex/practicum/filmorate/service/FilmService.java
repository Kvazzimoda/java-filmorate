package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreController genreController;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService, GenreController genreController) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreController = genreController;
    }

    public Film addFilm(Film film) {
        // Проверка MPA
        if (film.getMpa() != null && !EnumSet.allOf(MpaRating.class).contains(film.getMpa())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid MPA rating");
        }

        // Проверка жанров
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (!isValidGenre(genre.getId())) { // Предположим, есть метод проверки жанра
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid genre ID: " + genre.getId());
                }
            }
        }

        // Логика добавления фильма
        return filmStorage.addFilm(film);
    }

    // Метод проверки валидности жанра
    private boolean isValidGenre(int genreId) {
        List<Genre> allGenres = genreController.getAll(); // Получаем список всех жанров
        return allGenres.stream().anyMatch(genre -> genre.getId() == genreId);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            return null;
        }
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        User user = userService.getUserOrThrow(userId); // Предполагаем, что есть доступ к UserService
        film.getLikes().add(userId);
        // Обновляем фильм в хранилище
        updateFilm(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        User user = userService.getUserOrThrow(userId);
        film.getLikes().remove(userId);
        // Обновляем фильм в хранилище
        updateFilm(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmOrThrow(Integer id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Film with ID " + id + " not found"
                ));
    }
}