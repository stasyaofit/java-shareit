package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto addItemRequest(@RequestHeader(REQUEST_HEADER) Long userId,
                                             @RequestBody ItemRequestShortDto dto) {
        log.info("Получен POST-запрос  к эндпоинту: /requests на добавление вещи по запросу - {}" +
                " пользователем с id = {} .", dto, userId);
        return itemRequestService.addItemRequest(userId, dto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwnerRequests(@RequestHeader(REQUEST_HEADER) Long ownerId) {
        log.info("Получен GET-запрос к эндпоинту: /requests на получение списка запросов на добавление вещей" +
                "пользователем с id = {} .", ownerId);
        return itemRequestService.getOwnRequests(ownerId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(@RequestHeader(REQUEST_HEADER) Long userId,
                                                       @RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "20") Integer size) {
        log.info("Получен GET-запрос к эндпоинту: /requests/all/ на получение списка всех запросов на добавление вещей" +
                "пользователем с id = {} .", userId);
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(@RequestHeader(REQUEST_HEADER) Long userId,
                                         @PathVariable Long requestId) {
        log.info("Получен GET-запрос к эндпоинту: /requests/{requestId} на получение запроса на добавление вещи" +
                "с id = {} .", requestId);
        return itemRequestService.getRequestById(userId, requestId);
    }
}
