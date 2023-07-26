package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{id}")
    public ItemBookingCommentDto getItemById(@RequestHeader(REQUEST_HEADER) Long userId, @PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту: /items/{id} на получение вещи с id = {} .", id);
        return itemService.getItemById(userId, id);
    }

    @GetMapping
    public List<ItemBookingCommentDto> getOwnerItems(@RequestHeader(REQUEST_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: /items на получение вещей пользователя с id = {} .", ownerId);
        return itemService.getOwnerItems(ownerId);
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

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @PathVariable Long itemId, @RequestBody @Valid CommentDto commentDto) {
        log.info("Получен POST-запрос к эндпоинту: /items/{itemId}/comment на добавление отзыва " +
                "для вещи с id = {} пользователем с id = {} .", itemId, userId);
        return itemService.addComment(commentDto, itemId, userId);
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
