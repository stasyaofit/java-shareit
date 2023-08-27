package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto saveItem(ItemDto itemDto, Long userId);

    ItemDto saveItem(ItemDto itemDto, Long userId, Long requestId);

    ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId);

    void deleteItem(Long itemId);

    ItemBookingCommentDto getItemById(Long userID, Long itemId);

    List<ItemBookingCommentDto> getOwnerItems(Long ownerId, Integer from, Integer size);

    List<ItemDto> findAvailableItemsByText(String text, Integer from, Integer size);

    CommentResponseDto addComment(CommentDto dto, Long itemId, Long userId);
}
