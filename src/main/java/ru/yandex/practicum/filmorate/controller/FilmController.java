package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.addFilm(film);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm); // 201 вместо 200
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
        Film updatedFilm = filmService.updateFilm(film);
        if (updatedFilm != null) {
            return ResponseEntity.ok(updatedFilm);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film not found");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable Integer id) {
        try {
            Film film = filmService.getFilmOrThrow(id);
            return ResponseEntity.ok(film);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film with ID " + id + " not found");
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        try {
            filmService.addLike(id, userId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Integer id, @PathVariable Integer userId) {
        try {
            filmService.removeLike(id, userId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | ResponseStatusException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }

    // Обработка ошибок валидации
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage); // 400
    }

    // Обработка общих исключений (например, ошибки в сервисе)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralExceptions(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
    }
}