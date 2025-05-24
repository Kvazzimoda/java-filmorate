package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        if (film.getMpa() != null && !isValidMpa(film.getMpa())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid MPA rating: " + film.getMpa());
        }

        // Проверка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = film.getGenres().stream().distinct().collect(Collectors.toSet());
            for (Genre genre : uniqueGenres) {
                if (!isValidGenre(genre.getId())) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid genre ID: " + genre.getId());
                }
            }
            film.setGenres(uniqueGenres); // Устанавливаем уникальные жанры
        }

        // Логика добавления фильма
        try {
            Film savedFilm = filmStorage.addFilm(film);
            log.info("Film created with ID: {}", savedFilm.getId());
            return savedFilm;
        } catch (Exception e) {
            log.error("Failed to save film: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save film");
        }
    }

    // Метод проверки валидности жанра
    private boolean isValidGenre(int genreId) {
        List<Genre> allGenres = genreController.getAll(); // Получаем список всех жанров
        return allGenres.stream().anyMatch(genre -> genre.getId() == genreId);
    }

    // Метод проверки валидности MPA (обновлён для MpaRating)
    private boolean isValidMpa(MpaRating mpa) {
        return mpa != null && EnumSet.allOf(MpaRating.class).contains(mpa);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film with ID " + film.getId() + " not found");
        }

        // Проверка MPA
        if (film.getMpa() != null && !isValidMpa(film.getMpa())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid MPA rating: " + film.getMpa());
        }

        // Проверка жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = film.getGenres().stream().distinct().collect(Collectors.toSet());
            for (Genre genre : uniqueGenres) {
                if (!isValidGenre(genre.getId())) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid genre ID: " + genre.getId());
                }
            }
            film.setGenres(uniqueGenres);
        }

        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        User user = userService.getUserOrThrow(userId);
        film.getLikes().add(userId);
        updateFilm(film);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = getFilmOrThrow(filmId);
        User user = userService.getUserOrThrow(userId);
        film.getLikes().remove(userId);
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