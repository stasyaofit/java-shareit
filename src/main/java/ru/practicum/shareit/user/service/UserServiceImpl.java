package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsMailException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto createUser(UserDto userDto) {
        /* можно ли сюда вынести проверку почты следующим образом
        isMailUsed(userStorage.getNexId(), userDto.getEmail()

         */
        return UserMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User updateUser = userStorage.getUser(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
        log.info("Обновляемый пользователь {}", updateUser);
        updateUser.setName(userDto.getName() != null ? userDto.getName() : updateUser.getName());
        updateUser.setEmail(userDto.getEmail() != null && isMailUsed(userId, userDto.getEmail()) ? userDto.getEmail() : updateUser.getEmail());
        validateUser(updateUser);

        log.info("Пользователь {} обновлен.", updateUser);
        return UserMapper.toUserDto(userStorage.updateUser(updateUser));
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userStorage.deleteUser(userId)) {
            throw new UserNotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        log.info("Пользователь с id = {} успешно удалён.", userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userStorage.getUser(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return UserMapper.toUserDtoList(userStorage.findAll());
    }

    private void validateUser(User user) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private boolean isMailUsed(Long id, String email) {
        boolean isMailUsed = userStorage.findAll().stream()
                .filter(user -> !user.getId().equals(id))
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));

        if (isMailUsed) {
            throw new AlreadyExistsMailException("Такая почта уже существует");
        }
        return true;
    }
}
