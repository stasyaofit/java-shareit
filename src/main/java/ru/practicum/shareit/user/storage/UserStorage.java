package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User createUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long userId);

    List<User> findAll();

    Optional<User> getUser(Long userId);

}
