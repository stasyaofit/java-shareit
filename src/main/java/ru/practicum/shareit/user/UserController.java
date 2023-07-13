package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

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
    public UserDto createUser(@RequestBody @Valid UserDto userDto) {
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
