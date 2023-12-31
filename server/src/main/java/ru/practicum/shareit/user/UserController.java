package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable Long userId) {
        log.info("Получен GET-запрос к эндпоинту: /users/{userId} на получение пользователя с ID = {}.", userId);
        return userService.getUser(userId);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Получен GET-запрос к эндпоинту: /users на получение списка всех пользователей.");
        return userService.findAll();
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserDto userDto) {
        log.info("Получен POST-запрос к эндпоинту: /users на добавление пользователя");
        return userService.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto updateUser(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Получен PATCH-запрос к эндпоинту: /users/{userId} на обновления данных пользователя с id = {}.", userId);
        return userService.updateUser(userDto, userId);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        log.info("Получен DELETE-запрос к эндпоинту: /users/{userId} на удаление пользователя с id = {}.", userId);
        userService.deleteUser(userId);
    }
}
