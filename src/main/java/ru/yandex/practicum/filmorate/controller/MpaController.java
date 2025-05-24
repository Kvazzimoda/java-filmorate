package ru.yandex.practicum.filmorate.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    // Класс для представления MPA с id и name
    @Data
    public static class Mpa {
        private int id;
        private String name;

        public Mpa(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // Преобразуем MpaRating в список объектов Mpa с id
    private List<Mpa> getAllMpaRatings() {
        return Arrays.stream(MpaRating.values())
                .map(mpa -> new Mpa(mpa.ordinal() + 1, mpa.name()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<Mpa> getAll() {
        List<Mpa> mpaList = getAllMpaRatings();
        if (mpaList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No MPA ratings found");
        }
        return mpaList; // Должно вернуть 5 элементов (G, PG, PG_13, R, NC_17)
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        List<Mpa> mpaList = getAllMpaRatings();
        return mpaList.stream()
                .filter(mpa -> mpa.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MPA with ID " + id + " not found"));
    }
}