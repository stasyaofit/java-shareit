package ru.practicum.shareit.user.storage;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.AlreadyExistsMailException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Data
@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public User createUser(User user) {
        user.setId(nextId);
        isMailUsed(user.getId(), user.getEmail());
        nextId++;
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
    public boolean deleteUser(Long userId) {
        return users.remove(userId) != null;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUser(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    private void isMailUsed(Long id, String email) {
        boolean isMailUsed = users.values().stream()
                .filter(user -> !user.getId().equals(id))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (isMailUsed) {
            throw new AlreadyExistsMailException("Такая почта уже существует");
        }
    }
}
