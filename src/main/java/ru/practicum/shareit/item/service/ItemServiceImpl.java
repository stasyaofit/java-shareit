package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentResponseDtoMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.OperationAccessException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemDtoMapper itemMapper;
    private final BookingDtoMapper bookingMapper;
    private final CommentResponseDtoMapper commentMapper;

    @Override
    public ItemDto saveItem(ItemDto itemDto, Long userId) {
        User user = checkUserExistAndGet(userId);
        Item item = itemMapper.toItem(itemDto);
        item.setOwner(user);
        log.info("Вещь {} создана.", item);
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, Long itemId, Long userId) {
        Item updateItem = checkItemExistAndGet(itemId);
        log.info("Обновляемая вещь {}", updateItem);
        if (!Objects.equals(updateItem.getOwner().getId(), userId)) {
            throw new OperationAccessException("Пользователь с id = " + userId + " не является собственником вещи.");
        }
        updateItem.setName(itemDto.getName() != null ? itemDto.getName() : updateItem.getName());
        updateItem.setDescription(itemDto.getDescription() != null ? itemDto.getDescription() : updateItem.getDescription());
        updateItem.setAvailable(itemDto.getAvailable() != null ? itemDto.getAvailable() : updateItem.getAvailable());
        log.info("Вещь {} обновлена.", updateItem);
        return itemMapper.toItemDto(itemRepository.save(updateItem));
    }

    @Override
    public void deleteItem(Long itemId) {
        Item item = checkItemExistAndGet(itemId);
        itemRepository.delete(item);
        log.info("Вещь с id = {} удалена.", itemId);
    }

    @Override
    public ItemBookingCommentDto getItemById(Long userId, Long itemId) {
        Item item = checkItemExistAndGet(itemId);
        checkUserExistAndGet(userId);
        ItemBookingCommentDto itemDto;
        if (item.getOwner().getId().equals(userId)) {
            itemDto = itemMapper.toItemWithBookings(item, getItemLastBooking(itemId), getItemNextBooking(itemId));
            itemDto.setComments(getItemComments(itemId));
            return itemDto;
        }
        itemDto = itemMapper.toItemWithBookings(item, null, null);
        itemDto.setComments(getItemComments(itemId));
        return itemDto;
    }

    @Override
    public List<ItemBookingCommentDto> getOwnerItems(Long ownerId) {
        checkUserExistAndGet(ownerId);
        List<Item> items = itemRepository.findItemByOwner_IdIs(ownerId);
        List<ItemBookingCommentDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            ItemBookingCommentDto dto = itemMapper.toItemWithBookings(item, getItemLastBooking(item.getId()),
                    getItemNextBooking(item.getId()));
            dto.setComments(getItemComments(item.getId()));
            itemsDto.add(dto);
        }
        log.info("Список вещей пользователя c id = {}: {}", ownerId, itemsDto);
        return itemsDto.stream().
                sorted(Comparator.comparing(ItemBookingCommentDto::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> findAvailableItemsByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemMapper.toItemDtoList(
                itemRepository.findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(text, text));
    }

    @Override
    public CommentResponseDto addComment(CommentDto dto, Long itemId, Long userId) {
        Booking booking = bookingRepository.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(userId, itemId,
                LocalDateTime.now(), Status.APPROVED).orElseThrow(
                () -> new BadRequestException("Пользователь с id = " + userId + " не арендовал вещь."));
        User author = booking.getBooker();
        Item item = booking.getItem();
        Comment comment = commentRepository.save(commentMapper.toComment(dto, author, item, LocalDateTime.now()));
        return commentMapper.toCommentResponseDto(comment);
    }

    private Item checkItemExistAndGet(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Вещь с id = " + itemId + " не найдена."));
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private BookingBookerDto getItemLastBooking(Long itemId) {
        return bookingMapper.toBookingBookerDto(
                bookingRepository.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                        itemId, LocalDateTime.now(), Status.APPROVED));
    }

    private BookingBookerDto getItemNextBooking(Long itemId) {
        return bookingMapper.toBookingBookerDto(
                bookingRepository.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        itemId, LocalDateTime.now(), Status.APPROVED));
    }

    private List<CommentResponseDto> getItemComments(Long itemId) {
        return commentRepository.findByItem_Id(itemId).stream()
                .map(commentMapper::toCommentResponseDto).collect(Collectors.toList());
    }
}

