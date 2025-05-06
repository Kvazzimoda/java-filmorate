package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.GlobalExceptionHandler;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User createdUser = userService.addUser(user);
        URI location = URI.create("/users/" + createdUser.getId());  // URI к созданному ресурсу
        return ResponseEntity.created(location).body(createdUser);  // Возвращаем статус 201
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.getAllUsers();
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            // Возвращаем 400 с телом ошибки, если ID не указан
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }

        User updatedUser = userService.updateUser(user);

        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);  // Возвращаем обновленного пользователя
        } else {
            // Если пользователь не найден, выбрасываем исключение с 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        return userService.getUserOrThrow(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Integer id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Integer id, @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Integer id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Integer id, @PathVariable int otherId) {
        return userService.getCommonFriends(id, otherId);
    }

}