package ru.yandex.practicum.filmorate.storage.memory;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.user.impl.UserDbStorage;

import java.util.*;

/**
 * In-memory storage for users.
 * @deprecated Use {@link UserDbStorage} for production-ready database storage.
 */
@Deprecated(since = "1.0", forRemoval = true)
public abstract class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int idCounter = 0;

    @Override
    public User addUser(User user) {
        user.setId(++idCounter);
        // Если имя пустое — подставляем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            return null;
        }
        // Повторяем ту же логику: если имя пустое — подставляем логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }
}
