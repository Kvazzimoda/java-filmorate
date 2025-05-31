package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FilmControllerTest {

    @Mock
    private FilmService filmService;  // Мокаем зависимость FilmService

    @InjectMocks
    private FilmController filmController;  // Инжектим мок в контроллер

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(filmController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Поддержка LocalDate
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        Film film = new Film(0, "", "Description", LocalDate.of(2000, 1, 1), 120,
                new Mpa(), Set.of());
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(201);
        Film film = new Film(0, "Valid Name", longDescription, LocalDate.of(2000, 1, 1), 120,
                new Mpa(), Set.of());
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenReleaseDateIsBefore1895() throws Exception {
        Film film = new Film(0, "Valid Name", "Description", LocalDate.of(1895, 12, 27), 120,
                new Mpa(), Set.of());
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDurationIsZeroOrNegative() throws Exception {
        Film film = new Film(0, "Valid Name", "Description", LocalDate.of(2000, 1, 1), 0,
                new Mpa(), Set.of());
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenDescriptionIsExactly200() throws Exception {
        String exactDescription = "a".repeat(200);
        Film film = new Film(0, "Valid Name", exactDescription,
                LocalDate.of(2000, 1, 1), 120,
                new Mpa(1, "G"), Set.of());

        when(filmService.addFilm(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated());
    }

    @Test
    public void shouldAddFilmWithValidData() throws Exception {
        // Подготовка
        Film inputFilm = new Film(null, "Test Film", "Test Description",
                LocalDate.of(2023, 1, 1), 120,
                new Mpa(1, "G"), Set.of(new Genre(1, "Комедия")));
        Film savedFilm = new Film(1, inputFilm.getName(), inputFilm.getDescription(),
                inputFilm.getReleaseDate(), inputFilm.getDuration(),
                inputFilm.getMpa(), inputFilm.getGenres());

        when(filmService.addFilm(any(Film.class))).thenReturn(savedFilm);

        // Выполнение + Проверки
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldLikeAndUnlikeFilm() throws Exception {
        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/films/1/like/2"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPopularFilmsDefaultCount() throws Exception {
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetPopularFilmsWithCount() throws Exception {
        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetFilmById() throws Exception {
        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk());
    }

}