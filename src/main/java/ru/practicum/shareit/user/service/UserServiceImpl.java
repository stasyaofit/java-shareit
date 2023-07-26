package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userRepository.save(userDtoMapper.toUser(userDto));
        log.info("Добавлен пользователь {}", user);
        return userDtoMapper.toUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto, Long userId) {
        User updateUser = checkUserExistAndGet(userId);
        log.info("Обновляемый пользователь {}", updateUser);
        updateUser.setName(userDto.getName() != null ? userDto.getName() : updateUser.getName());
        String oldEmail = updateUser.getEmail();
        if (!oldEmail.equals(userDto.getEmail())) {
            updateUser.setEmail(userDto.getEmail() != null ? userDto.getEmail() : oldEmail);
        }
        log.info("Пользователь {} обновлен.", updateUser);
        return userDtoMapper.toUserDto(userRepository.save(updateUser));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User user = checkUserExistAndGet(userId);
        userRepository.delete(user);
        log.info("Пользователь с id = {} успешно удалён.", userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = checkUserExistAndGet(userId);
        return userDtoMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> findAll() {
        return userDtoMapper.toUserDtoList(userRepository.findAll());
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
    }
}
