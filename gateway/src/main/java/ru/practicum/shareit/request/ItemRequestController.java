package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(REQUEST_HEADER) Long userId,
                                                    @RequestBody @Valid ItemRequestShortDto dto) {
        log.info("Получен POST-запрос  к эндпоинту: /requests на добавление вещи по запросу - {}" +
                " пользователем с id = {} .", dto, userId);
        return requestClient.addItemRequest(userId, dto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerRequests(@RequestHeader(REQUEST_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: /requests на получение списка запросов на добавление вещей" +
                "пользователем с id = {} .", ownerId);
        return requestClient.getOwnerRequests(ownerId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(REQUEST_HEADER) Long userId,
                                                               @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                                               @RequestParam(name = "size", defaultValue = "20") @Positive Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /requests/all/ на получение списка всех запросов на добавление вещей" +
                "пользователем с id = {} .", userId);
        return requestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@RequestHeader(REQUEST_HEADER) Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Получен GET-запрос к эндпоинту: /requests/{requestId} на получение запроса на добавление вещи" +
                "с id = {} .", requestId);
        return requestClient.getRequestById(requestId, userId);
    }
}