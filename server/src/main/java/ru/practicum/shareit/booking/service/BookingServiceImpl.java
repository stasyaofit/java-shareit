package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingDtoMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repositoty.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingDtoMapper dtoMapper;

    @Override
    public BookingResponseDto addBooking(BookingRequestDto bookingDto, Long bookerId) {
        User booker = checkUserExistAndGet(bookerId);
        Item item = checkItemExistAndGetAvailable(bookingDto.getItemId());
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new NotFoundException("Владелец вещи не может бронировать свои вещи.");
        }
        Booking booking = dtoMapper.mapToBooking(bookingDto, item, booker, Status.WAITING);
        log.info("Бронирование веши {} ожидает подтверждения от владельца", item);
        return dtoMapper.mapToBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved) {
        checkUserExistAndGet(ownerId);
        Booking booking = checkBookingExistAndGet(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи."); // postman требует 404
        }
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new BadRequestException("Бронь с id = " + bookingId + " не ожидает подтверждения");
        }
        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return dtoMapper.mapToBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        checkUserExistAndGet(userId);
        Booking booking = checkBookingExistAndGet(bookingId);
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (userId.equals(ownerId) || userId.equals(bookerId)) {
            return dtoMapper.mapToBookingResponseDto(booking);
        } else {
            throw new NotFoundException("Бронь с id = " + bookingId + " для пользователя с id = " + userId + " не найдена");
        }
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long bookerId, BookingState state, Integer from, Integer size) {
        checkUserExistAndGet(bookerId);
        LocalDateTime now = LocalDateTime.now();
        Pageable page = PageRequest.of((int) from / size, size);
        Pageable sortPage = PageRequest.of((int) from / size, size, Sort.by(Sort.Direction.ASC, "start"));
        List<Booking> userBookings;
        switch (state) {
            case ALL:
                userBookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId, page);
                log.info("Все бронирования: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
            case CURRENT:
                userBookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId,
                        now, now, sortPage);
                log.info("Текущие бронирования: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
            case PAST:
                userBookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now, page);
                log.info("Завершённые бронирования: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
            case FUTURE:
                userBookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(bookerId, now, page);
                log.info("Будующие бронирования: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
            case WAITING:
                userBookings = bookingRepository.findByBookerIdAndStatusEquals(bookerId, Status.WAITING, page);
                log.info("Бронирования, ожидающие подтверждения: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
            case REJECTED:
                userBookings = bookingRepository.findByBookerIdAndStatusEquals(bookerId, Status.REJECTED, page);
                log.info("Отклонённые бронирования: {}.", userBookings);
                return dtoMapper.mapToBookingResponseDtoList(userBookings);
        }
        return Collections.emptyList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, Integer from, Integer size) {
        User owner = checkUserExistAndGet(ownerId);
        LocalDateTime now = LocalDateTime.now();
        Pageable page = PageRequest.of((int) from / size, size);
        List<Booking> ownerBookings;
        switch (state) {
            case ALL:
                ownerBookings = bookingRepository.findByItem_OwnerOrderByStartDesc(owner, page);
                log.info("Все бронирования: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
            case CURRENT:
                ownerBookings = bookingRepository.findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner, now,
                        now, page);
                log.info("Текущие бронирования: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
            case PAST:
                ownerBookings = bookingRepository.findByItem_OwnerAndEndBeforeOrderByStartDesc(owner, now, page);
                log.info("Завершённые бронирования: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
            case FUTURE:
                ownerBookings = bookingRepository.findByItem_OwnerAndStartAfterOrderByStartDesc(owner, now, page);
                log.info("Будующие бронирования: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
            case WAITING:
                ownerBookings = bookingRepository.findByItem_OwnerAndStatusEquals(owner, Status.WAITING, page);
                log.info("Бронирования, ожидающие подтверждения: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
            case REJECTED:
                ownerBookings = bookingRepository.findByItem_OwnerAndStatusEquals(owner, Status.REJECTED, page);
                log.info("Отклонённые бронирования: {}.", ownerBookings);
                return dtoMapper.mapToBookingResponseDtoList(ownerBookings);
        }
        return Collections.emptyList();
    }

    private Item checkItemExistAndGetAvailable(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException("Вещь с id = " + itemId + " не найдена."));
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь недоступна.");
        }
        return item;
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Booking checkBookingExistAndGet(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(
                () -> new UserNotFoundException("Бронь с id = " + bookingId + " не найдена."));
    }
}
