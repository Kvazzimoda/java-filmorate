package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpaControllerTest {

    @Mock
    private MpaService mpaService;

    @InjectMocks
    private MpaController mpaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mpaController).build();
    }

    @Test
    void shouldReturnAllMpaRatings() throws Exception {
        when(mpaService.findAll()).thenReturn(List.of(
                new Mpa(1, "G"),
                new Mpa(2, "PG")
        ));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("G"));
    }

    @Test
    void shouldReturnMpaById() throws Exception {
        when(mpaService.findById(1)).thenReturn(new Mpa(1, "G"));

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("G"));
    }
}
