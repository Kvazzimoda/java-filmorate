package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
public class GenreController {

    // Предположим, что это список жанров (в реальном приложении это может быть БД)
    private final List<Genre> genres = Arrays.asList(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    @GetMapping
    public List<Genre> getAll() {
        if (genres.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No genres found");
        }
        return genres; // Должно вернуть 6 элементов
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return genres.stream()
                .filter(genre -> genre.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre with ID " + id + " not found"));
    }
}