package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.net.URI;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.addFilm(film);
        return ResponseEntity.created(URI.create("/films/" + createdFilm.getId()))
                .body(createdFilm);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.getAllFilms();
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Film ID is required");
        }
        return ResponseEntity.ok(filmService.updateFilm(film));
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Integer id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}