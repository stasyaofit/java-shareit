package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private static final String REQUEST_HEADER = "X-Sharer-User-Id";

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    public ItemDto getItemById(@RequestHeader(REQUEST_HEADER) @PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту: /items/{id} на получение вещи с id = {} .", id);
        return itemService.getItemById(id);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader(REQUEST_HEADER) Long userId) {
        log.info("Получен GET-запрос к эндпоинту: /items на получение вещей пользователя с id = {} .", userId);
        return itemService.getUserItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> findAvailableItemsByText(@RequestParam String text) {
        log.info("Получен GET-запрос к эндпоинту: /items/search?text={} на поиск доступных вещей по строке.", text);
        return itemService.findAvailableItemsByText(text);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(REQUEST_HEADER) Long userId, @RequestBody @Valid ItemDto itemDto) {
        log.info("Получен POST-запрос к эндпоинту: /items на создание вещи пользователем с id = {} .", userId);
        return itemService.saveItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(REQUEST_HEADER) Long userId,
                              @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        log.info("Получен PATCH-запрос к эндпоинту: /items/{itemId} на обновление вещи пользователем с id = {} .", userId);
        return itemService.updateItem(itemDto, itemId, userId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable Long itemId) {
        log.info("Получен DELETE-запрос к эндпоинту: /items/{itemId} на удаление вещи с id = {} .", itemId);
        itemService.deleteItem(itemId);
    }
}
