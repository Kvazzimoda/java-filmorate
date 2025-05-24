package ru.yandex.practicum.filmorate.model;

public interface FriendshipStorage {
    void createFriendship(Integer userId, Integer friendId);
    void removeFriendship(Integer userId, Integer friendId);
    boolean friendshipExists(Integer userId, Integer friendId);
}
