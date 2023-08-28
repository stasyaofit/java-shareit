package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{id}")
    public ItemBookingCommentDto getItemById(@RequestHeader(REQUEST_HEADER) Long userId, @PathVariable Long id) {
        log.info("Получен GET-запрос к эндпоинту: /items/{id} на получение вещи с id = {} .", id);
        return itemService.getItemById(userId, id);
    }

    @GetMapping
    public List<ItemBookingCommentDto> getOwnerItems(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                                     @RequestParam(defaultValue = "0") Integer from,
                                                     @RequestParam(defaultValue = "20") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /items на получение вещей пользователя с id = {} .", ownerId);
        return itemService.getOwnerItems(ownerId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> findAvailableItemsByText(@RequestParam String text,
                                                  @RequestParam(defaultValue = "0") Integer from,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /items/search?text={} на поиск доступных вещей по строке.", text);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemService.findAvailableItemsByText(text, from, size);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(REQUEST_HEADER) Long userId, @RequestBody ItemDto itemDto) {
        log.info("Получен POST-запрос к эндпоинту: /items на создание вещи пользователем с id = {} .", userId);
        if (itemDto.getRequestId() != null) {
            return itemService.saveItem(itemDto, userId, itemDto.getRequestId());
        }
        return itemService.saveItem(itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @PathVariable Long itemId, @RequestBody CommentDto commentDto) {
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
