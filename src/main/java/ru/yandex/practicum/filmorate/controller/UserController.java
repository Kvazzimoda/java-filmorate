package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.net.URI;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        log.info("Creating new user: {}", user.getEmail());
        User createdUser = userService.addUser(user);
        URI location = URI.create("/users/" + createdUser.getId());
        return ResponseEntity.created(location).body(createdUser);
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Request to get all users");
        return userService.getAllUsers();
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        log.info("Updating user with id: {}", user.getId());
        if (user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID is required");
        }
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Integer id) {
        log.info("Request to get user by id: {}", id);
        return userService.getUserOrThrow(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Integer id, @PathVariable int friendId) {
        log.info("Adding friend with id {} to user with id {}", friendId, id);
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Integer id, @PathVariable int friendId) {
        log.info("Removing friend with id {} from user with id {}", friendId, id);
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable Integer id) {
        log.info("Request to get friends for user with id: {}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Integer id, @PathVariable int otherId) {
        log.info("Request to get common friends for users with ids {} and {}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}