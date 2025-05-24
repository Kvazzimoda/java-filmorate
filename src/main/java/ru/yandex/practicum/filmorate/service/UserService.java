package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.FriendshipStorage;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserService(UserStorage userStorage, FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }


    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.getUserById(user.getId())
                .map(u -> userStorage.updateUser(user))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        // Создаем запись о дружбе
        friendshipStorage.createFriendship(userId, friendId);

        // Добавляем ID друга
        user.getFriendIds().add(friendId);
        friend.getFriendIds().add(userId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        friendshipStorage.removeFriendship(userId, friendId);
        userStorage.getUserById(userId).ifPresent(user ->
                user.getFriendIds().remove(friendId)
        );
        userStorage.getUserById(friendId).ifPresent(friend ->
                friend.getFriendIds().remove(userId)
        );
    }

    public List<User> getFriends(Integer userId) {
        return userStorage.getUserById(userId)
                .map(user -> user.getFriendIds().stream()
                        .map(this::getUserOrThrow)
                        .collect(Collectors.toList()))
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        Set<Integer> userFriends = getFriendIds(userId);
        Set<Integer> otherFriends = getFriendIds(otherId);

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(this::getUserOrThrow)
                .collect(Collectors.toList());
    }

    private Set<Integer> getFriendIds(Integer userId) {
        return userStorage.getUserById(userId)
                .map(User::getFriendIds)
                .orElse(Collections.emptySet());
    }

    public User getUserOrThrow(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
