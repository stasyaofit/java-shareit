package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestResponseDto addItemRequest(Long userId, ItemRequestShortDto dto);

    List<ItemRequestResponseDto> getOwnRequests(Long requesterId);

    List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size);

    ItemRequestResponseDto getRequestById(Long userId, Long requestId);
}
