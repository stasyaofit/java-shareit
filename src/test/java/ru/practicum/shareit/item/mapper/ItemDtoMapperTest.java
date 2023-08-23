package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class ItemDtoMapperTest {
    @Autowired
    private ItemDtoMapper itemDtoMapper;
    private final User owner = User.builder().id(1L).name("owner").email("owner@mail.com").build();
    private final Item item = Item.builder()
            .id(1L)
            .name("item")
            .description("description")
            .available(true)
            .owner(owner)
            .request(ItemRequest.builder().id(1L).build())
            .build();

    @Test
    void toItemDto() {
        ItemDto itemDto = itemDtoMapper.toItemDto(item);

        assertNotNull(itemDto);
        assertEquals("item", itemDto.getName());
        assertEquals("description", itemDto.getDescription());
        assertEquals(1L, itemDto.getId());
        assertEquals(true, itemDto.getAvailable());
        assertEquals(1L, itemDto.getRequestId());
    }

    @Test
    void toItem() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .requestId(1L)
                .build();

        Item item = itemDtoMapper.toItem(itemDto);

        assertNotNull(item);
        assertEquals("item", item.getName());
        assertEquals("description", item.getDescription());
        assertEquals(1L, item.getId());
        assertEquals(true, item.getAvailable());
        assertNull(item.getOwner());
    }

    @Test
    void toItemDtoList() {
        List<Item> items = List.of(item);
        List<ItemDto> itemDtoList = itemDtoMapper.toItemDtoList(items);
        ItemDto dto = itemDtoList.get(0);

        assertEquals(1, itemDtoList.size());
        assertEquals(1L, dto.getId());
        assertEquals("item", dto.getName());
        assertEquals("description", dto.getDescription());
        assertEquals(true, dto.getAvailable());
        assertEquals(1L, dto.getRequestId());
    }
}

