package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.RequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositoty.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestMapper requestMapper;
    private final ItemDtoMapper itemDtoMapper;


    @Override
    public ItemRequestResponseDto addItemRequest(Long userId, ItemRequestShortDto dto) {
        User requester = checkUserExistAndGet(userId);
        LocalDateTime created = LocalDateTime.now();
        ItemRequest itemRequest = requestMapper.fromShortDto(dto, requester, List.of(), created);
        requestRepository.save(itemRequest);
        log.info("Запрос " + dto.getDescription() + "добавлен пользователем с id = " + userId);
        return requestMapper.mapToRequestResponseDto(itemRequest, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long requesterId) {
        checkUserExistAndGet(requesterId);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId);
        // мапа id запроса -> список вещей, созданных по этому запросу (в формате dto)
        Map<Long, ItemForRequestDto> requestIdItemDtosMap = getItemsMadeForRequests(requests);
        log.info("Получен список собственных запросов на добавление вещей пользователем с id = " + requesterId);
        return mapItemRequestsToItemRequestResponseDto(requests, requestIdItemDtosMap);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        Sort sortByCreated = Sort.by(Sort.Direction.DESC, "created");
        Pageable page = PageRequest.of((int) from / size, size, sortByCreated);
        List<ItemRequest> requests = requestRepository.findAllByRequesterIdNot(userId, page);
        // мапа id запроса -> список вещей, созданных по этому запросу (в формате dto)
        Map<Long, ItemForRequestDto> requestIdMapItemDto = getItemsMadeForRequests(requests);
        return mapItemRequestsToItemRequestResponseDto(requests, requestIdMapItemDto);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        checkUserExistAndGet(userId);
        ItemRequest itemRequest = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запроc c id = " + requestId + " не найден."));
        // мапа id запроса -> список вещей, созданных по этому запросу (в формате dto)
        Map<Long, ItemForRequestDto> requestIdItemDtosMap = getItemsMadeForRequests(List.of(itemRequest));
        return requestMapper.mapToRequestResponseDto(itemRequest, new ArrayList<>(requestIdItemDtosMap.values()));
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Map<Long, ItemForRequestDto> getItemsMadeForRequests(List<ItemRequest> requests) {
        return itemRepository.findAllByRequest_IdIn(requests.stream()
                        .map(ItemRequest::getId)
                        .collect(Collectors.toList()))
                .stream()
                .collect(toMap(item -> item.getRequest().getId(), itemDtoMapper::mapItemToItemForRequest));
    }

    private List<ItemRequestResponseDto> mapItemRequestsToItemRequestResponseDto(List<ItemRequest> requests,
                                                                Map<Long, ItemForRequestDto> items) {
        return requests.stream()
                .map(r -> requestMapper.mapToRequestResponseDto(
                        r, items.containsKey(r.getId()) ? List.of(items.get(r.getId())) : List.of()))
                .collect(Collectors.toList());
    }
}

