package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()).isEmpty()) {
            return null; // возвращаем null, если такого пользователя нет
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        // Добавляем запись о дружбе с статусом UNCONFIRMED
        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
    }


    public void removeFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getFriends(Integer userId) {
        return getUserOrThrow(userId).getFriends().keySet().stream()
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public List<User> getConfirmedFriends(Integer userId) {
        return getUserOrThrow(userId).getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    public User getUserOrThrow(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
