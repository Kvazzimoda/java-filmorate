package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    @Transactional
    public void addFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.addFriend(userId, friendId);
    }

    @Transactional
    public void removeFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Collection<User> userFriends = userStorage.getFriends(userId);
        Collection<User> otherFriends = userStorage.getFriends(otherId);
        return userFriends.stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toList());
    }

    public User getUserOrThrow(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

}