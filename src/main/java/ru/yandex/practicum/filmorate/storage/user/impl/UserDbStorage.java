package ru.yandex.practicum.filmorate.storage.user.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.mapper.UserMapper;

import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(UserDbStorage.class);
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, userMapper, id).stream().findFirst();
    }

    @Override
    public Collection<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", userMapper);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (!getUserById(userId).isPresent() || !getUserById(friendId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (!getUserById(userId).isPresent() || !getUserById(friendId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        if (!getUserById(userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, userMapper, userId);
    }

    @Override
    public Collection<Integer> getFriendId(int userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, userId);
    }
}