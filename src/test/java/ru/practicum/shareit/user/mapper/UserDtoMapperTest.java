package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserDtoMapperTest {
    @Autowired
    private UserDtoMapper userDtoMapper;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("name").email("test@mail.com").build();
    }

    @Test
    void toUserDto() {
        UserDto userDto = userDtoMapper.toUserDto(user);

        assertNotNull(userDto);
        assertEquals("name", userDto.getName());
        assertEquals("test@mail.com", userDto.getEmail());
    }

    @Test
    void toUser() {
        UserDto userDto = UserDto.builder().id(1L).name("name").email("test@mail.com").build();
        User user = userDtoMapper.toUser(userDto);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("name", user.getName());
        assertEquals("test@mail.com", user.getEmail());
    }

    @Test
    void toUserDtoList() {
        List<User> users = List.of(user);
        List<UserDto> userDtoList = userDtoMapper.toUserDtoList(users);

        assertEquals(1, userDtoList.size());
        assertEquals(1L, userDtoList.get(0).getId());
        assertEquals("name", userDtoList.get(0).getName());
        assertEquals("test@mail.com", userDtoList.get(0).getEmail());
    }
}

