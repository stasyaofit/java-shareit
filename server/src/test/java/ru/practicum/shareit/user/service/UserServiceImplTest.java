package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {
    @InjectMocks
    private final UserServiceImpl userService;
    @MockBean
    private final UserRepository userRepository;
    @Autowired
    private final UserDtoMapper mapper;
    private final User user = User.builder().id(1L).name("testName").email("test@mail.ru").build();
    private UserDto requestDto;
    private UserDto responseDto;

    @Test
    @DisplayName("Создание валидного пользователя")
    void createUser_whenDataOk_thenOk() {
        requestDto = UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        responseDto = userService.createUser(requestDto);

        assertEquals(user.getName(), responseDto.getName());
        assertEquals(user.getEmail(), responseDto.getEmail());
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными")
    void updateUser_whenUserValid_thenUpdatedUser() {
        long userId = user.getId();
        requestDto = UserDto.builder()
                .name("updateName")
                .email("updated@mail.com").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        responseDto = userService.updateUser(requestDto, userId);

        assertEquals(user.getName(), responseDto.getName());
        assertEquals("updated@mail.com", responseDto.getEmail());
        assertEquals(user.getId(), responseDto.getId());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя")
    void patchUser_whenNotFound_thenNotFound() {
        requestDto = UserDto.builder()
                .email("updated@mail.com")
                .build();
        Mockito.when(userRepository.findById(any())).thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(requestDto, user.getId())
        );

        assertEquals("Пользователь с id = " + user.getId() + " не найден.", e.getMessage());
    }

    @Test
    @DisplayName("Получение созданного пользователя по id")
    void getUserById_whenUserFound_thenReturnedUser() {
        long userId = user.getId();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        responseDto = userService.getUser(userId);

        assertThat(mapper.toUserDto(user), equalTo(responseDto));
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Получение несуществующего пользователь по id, когда пользователь не найден, " +
            "тогда выбрасывается исключение")
    void getUserById_whenUserNotFound_thenExceptionThrown() {
        long userId = 0L;
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUser(userId));

        assertThat("Пользователь с id = 0 не найден.", equalTo(exception.getMessage()));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Получение всех пользователей, когда их не существует, возвращает пустой список")
    void getAllUsers_whenUsersNotExists_thenReturnedEmptyList() {
        List<UserDto> expectedUsers = userService.findAll();

        assertThat(expectedUsers, empty());
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Получение списка все пользователей, если пользователи существуют, то получен непустой список.")
    void getAllUsers_whenUserExists_thenReturnedUsersListNotEmpty() {
        List<User> expectedUsers = List.of(user);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        List<UserDto> actualUsers = userService.findAll();

        assertThat(mapper.toUserDtoList(expectedUsers), equalTo(actualUsers));
        assertThat(actualUsers.get(0).getId(), equalTo(user.getId()));
        assertThat(actualUsers.get(0).getName(), equalTo(user.getName()));
        assertThat(actualUsers.get(0).getEmail(), equalTo(user.getEmail()));
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Удаление существующего пользователя.")
    void deleteUserExist() {
        long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя.")
    void deleteUserNotExist_thenExceptionThrown() {
        long userId = user.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(userId));

        assertThat("Пользователь с id = 1 не найден.", equalTo(exception.getMessage()));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(user);
        verifyNoMoreInteractions(userRepository);
    }
}

