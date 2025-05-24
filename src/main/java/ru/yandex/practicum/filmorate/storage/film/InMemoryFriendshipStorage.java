package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStorage;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryFriendshipStorage implements FriendshipStorage {
    private final Map<Integer, Set<Integer>> friendships = new ConcurrentHashMap<>();

    @Override
    public void createFriendship(Integer userId, Integer friendId) {
        friendships.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friendships.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeFriendship(Integer userId, Integer friendId) {
        Optional.ofNullable(friendships.get(userId)).ifPresent(set -> set.remove(friendId));
        Optional.ofNullable(friendships.get(friendId)).ifPresent(set -> set.remove(userId));
    }

    @Override
    public boolean friendshipExists(Integer userId, Integer friendId) {
        return Optional.ofNullable(friendships.get(userId))
                .map(set -> set.contains(friendId))
                .orElse(false);
    }
}