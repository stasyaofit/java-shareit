package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;


@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> saveItem(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                           @RequestBody @Valid ItemDto dto) {
        log.info("Получен POST-запрос к эндпоинту: /items на создание вещи пользователем с id = {} .", ownerId);
        return itemClient.saveItem(ownerId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto dto) {
        log.info("Получен PATCH-запрос к эндпоинту: /items/{itemId} на обновление вещи пользователем с id = {} .", ownerId);
        return itemClient.updateItem(ownerId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                          @PathVariable Long itemId) {
        log.info("Получен GET-запрос к эндпоинту: /items/{id} на получение вещи с id = {} .", itemId);
        return itemClient.getItemById(ownerId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "20") @Positive Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /items на получение вещей пользователя с id = {} .", ownerId);
        return itemClient.getOwnerItems(ownerId, from, size);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long itemId) {
        log.info("Получен DELETE-запрос к эндпоинту: /items/{itemId} на удаление вещи с id = {} .", itemId);
        return itemClient.deleteItem(itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "20") @Positive Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /items/search?text={} на поиск доступных вещей по строке.", text);
        return itemClient.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(REQUEST_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid CommentDto dto) {
        log.info("Получен POST-запрос к эндпоинту: /items/{itemId}/comment на добавление отзыва " +
                "для вещи с id = {} пользователем с id = {} .", itemId, userId);
        return itemClient.addComment(dto, itemId, userId);
    }
}