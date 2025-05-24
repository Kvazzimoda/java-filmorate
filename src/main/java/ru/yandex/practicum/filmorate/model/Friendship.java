package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    private Integer userId;    // ID пользователя, которому принадлежит дружба
    private Integer friendId;  // ID друга
    private FriendshipStatus status;
}
