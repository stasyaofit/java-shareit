package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserDto createUser(UserDto userDto) {
        userStorage.isMailUsed(userDto.getEmail());
        return UserMapper.toUserDto(userStorage.createUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User updateUser = userStorage.getUser(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
        log.info("Обновляемый пользователь {}", updateUser);
        updateUser.setName(userDto.getName() != null ? userDto.getName() : updateUser.getName());
        String oldEmail = updateUser.getEmail();
        if (!oldEmail.equals(userDto.getEmail())) {
            updateUser.setEmail(userDto.getEmail() != null &&
                    userStorage.isMailUsed(userDto.getEmail()) ? userDto.getEmail() : updateUser.getEmail());
        }
        userStorage.updateUserEmails(oldEmail, updateUser.getEmail());
        log.info("Пользователь {} обновлен.", updateUser);
        return UserMapper.toUserDto(userStorage.updateUser(updateUser));
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.deleteUser(userId);
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
}
