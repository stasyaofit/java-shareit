package ru.practicum.shareit.user.storage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistsMailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Data
@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();
    private Long nextId = 1L;

    @Override
    public User createUser(User user) {
        user.setId(nextId++);
        userEmails.add(user.getEmail());
        users.put(user.getId(), user);
        log.info("Пользователь с id = {} успешно создан.", user.getId());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUser(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
        userEmails.remove(user.getEmail());
        users.remove(userId);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void updateUserEmails(String oldEmail, String newEmail) {
        userEmails.remove(oldEmail);
        userEmails.add(newEmail);
    }

    @Override
    public boolean isMailUsed(String email) {
        if (userEmails.contains(email)) {
            throw new AlreadyExistsMailException("Такая почта уже существует");
        }
        return true;
    }
}
