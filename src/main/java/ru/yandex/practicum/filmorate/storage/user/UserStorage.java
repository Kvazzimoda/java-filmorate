package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    // Основные методы работы с пользователями
    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(Integer id);

    Collection<User> getAllUsers();

    // Методы для работы с друзьями
    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    Collection<User> getFriends(int userId);

    Collection<Integer> getFriendId(int userId); // Для оптимизации запросов

}
