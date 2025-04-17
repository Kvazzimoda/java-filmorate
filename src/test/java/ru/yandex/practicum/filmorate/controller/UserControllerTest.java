package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Поддержка LocalDate
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        User user = new User(0, "", "validLogin", "Name", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        User user = new User(0, "invalid-email", "validLogin", "Name", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginIsBlank() throws Exception {
        User user = new User(0, "valid@email.com", "", "Name", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginHasSpaces() throws Exception {
        User user = new User(0, "valid@email.com", "login with space", "Name", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBirthdayIsInFuture() throws Exception {
        User user = new User(0, "valid@email.com", "validLogin", "Name", LocalDate.now().plusDays(1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() throws Exception {
        User user = new User(0, "valid@email.com", "validLogin", "", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    User createdUser = objectMapper.readValue(response, User.class);
                    assertEquals("validLogin", createdUser.getName());
                });
    }

    @Test
    void shouldCreateUserWithValidData() throws Exception {
        User user = new User(0, "valid@email.com", "validLogin", "Name", LocalDate.of(1990, 1, 1));
        String json = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

}