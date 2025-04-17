package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilmControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        FilmController filmController = new FilmController();
        mockMvc = MockMvcBuilders.standaloneSetup(filmController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Поддержка LocalDate
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        Film film = new Film(0, "", "Description", LocalDate.of(2000, 1, 1), 120);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(201);
        Film film = new Film(0, "Valid Name", longDescription, LocalDate.of(2000, 1, 1), 120);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenReleaseDateIsBefore1895() throws Exception {
        Film film = new Film(0, "Valid Name", "Description", LocalDate.of(1895, 12, 27), 120);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDurationIsZeroOrNegative() throws Exception {
        Film film = new Film(0, "Valid Name", "Description", LocalDate.of(2000, 1, 1), 0);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenDescriptionIsExactly200() throws Exception {
        String validDescription = "a".repeat(200);
        Film film = new Film(0, "Valid Name", validDescription, LocalDate.of(2000, 1, 1), 120);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddFilmWithValidData() throws Exception {
        Film film = new Film(0, "Valid Name", "Description", LocalDate.of(2000, 1, 1), 120);
        String json = objectMapper.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}