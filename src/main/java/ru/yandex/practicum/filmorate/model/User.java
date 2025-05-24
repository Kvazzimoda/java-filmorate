package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Integer id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным и содержать символ '@'")
    @Size(max = 100, message = "Email не может быть длиннее 100 символов")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    @Size(max = 50, message = "Логин не может быть длиннее 50 символов")
    private String login;
    /**
     * Имя пользователя. Если не указано или пустое, используется значение логина.
     */
    @Size(max = 100, message = "Имя не может быть длиннее 100 символов")
    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    private Map<Integer, FriendshipStatus> friends = new HashMap<>();

    // Добавляем конструктор для тестов
    public User(Integer id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }

}
