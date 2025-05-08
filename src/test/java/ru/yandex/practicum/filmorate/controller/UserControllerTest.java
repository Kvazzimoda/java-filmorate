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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;  // Мокаем зависимость UserService

    @InjectMocks
    private UserController userController;  // Инжектим мок в контроллер

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // Поддержка LocalDate
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
        // входящий пользователь с пустым именем
        User userInput = new User(0, "valid@email.com", "validLogin", "", LocalDate.of(1990, 1, 1));
        // ожидаемый результат — имя подставляется из логина
        User userOutput = new User(1, "valid@email.com", "validLogin", "validLogin", LocalDate.of(1990, 1, 1));

        // настройка поведения мока
        when(userService.addUser(any())).thenReturn(userOutput);

        String json = objectMapper.writeValueAsString(userInput);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    User createdUser = objectMapper.readValue(response, User.class);
                    assertEquals("validLogin", createdUser.getName());
                });
    }

    @Test
    void shouldCreateUserWithValidData() throws Exception {
        User inputUser = new User(0, "valid@email.com", "validLogin", "Name", LocalDate.of(1990, 1, 1));
        User createdUser = new User(1, "valid@email.com", "validLogin", "Name", LocalDate.of(1990, 1, 1));

        when(userService.addUser(any(User.class))).thenReturn(createdUser);

        String json = objectMapper.writeValueAsString(inputUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                .andExpect(jsonPath("$.name").value("Name"));
    }

    @Test
    void shouldAddAndRemoveFriend() throws Exception {
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetFriendsList() throws Exception {
        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetUserById() throws Exception {
        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

}