package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class RequestMapperTest {
    LocalDateTime created;

    @Autowired
    private RequestMapper mapper;

    private final User requester = User.builder().id(1L).name("requester").email("requester@mail.com").build();

    @BeforeEach
    void instanceTime() {
        created = LocalDateTime.now();
    }

    @Test
    void toRequestResponseDto() {
        //given
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .requester(requester)
                .description("description")
                .created(created).build();
        //when
        ItemRequestResponseDto itemRequestDto = mapper.mapToRequestResponseDto(itemRequest, List.of());
        //then
        assertNotNull(itemRequestDto);
        assertEquals("description", itemRequestDto.getDescription());
        assertEquals(created, itemRequestDto.getCreated());
        assertEquals(1L, itemRequestDto.getId());
    }

    @Test
    void fromDto() {
        ItemRequestShortDto itemRequestDto = ItemRequestShortDto.builder().description("description").build();

        ItemRequest itemRequest = mapper.fromShortDto(itemRequestDto, requester, List.of(), created);

        assertNotNull(itemRequest);
        assertEquals("description", itemRequest.getDescription());
        assertEquals(created, itemRequest.getCreated());
        assertNull(itemRequest.getId());
    }

}
