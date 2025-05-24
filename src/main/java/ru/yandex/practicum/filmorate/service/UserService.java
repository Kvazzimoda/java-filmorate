package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Friendship;
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
            return null;
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().add(new Friendship(friendId, FriendshipStatus.UNCONFIRMED));
        friend.getFriends().add(new Friendship(userId, FriendshipStatus.CONFIRMED));
    }

    public void removeFriend(int userId, int friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().removeIf(f -> f.getFriendId() == friendId);
        friend.getFriends().removeIf(f -> f.getFriendId() == userId);
    }

    public Collection<User> getFriends(int userId) {
        return getUserOrThrow(userId).getFriends().stream()
                .map(Friendship::getFriendId)
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriendIds = getUserOrThrow(userId).getFriends().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        Set<Integer> otherFriendIds = getUserOrThrow(otherId).getFriends().stream()
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        return userFriendIds.stream()
                .filter(otherFriendIds::contains)
                .map(userStorage::getUserById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public User getUserOrThrow(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

