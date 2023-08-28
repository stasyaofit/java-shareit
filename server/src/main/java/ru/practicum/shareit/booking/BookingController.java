package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import java.util.List;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto addBooking(@RequestHeader(REQUEST_HEADER) Long bookerId,
                                         @RequestBody BookingRequestDto bookingDto) {
        log.info("Получен POST-запрос к эндпоинту: /bookings на бронирование вещи " +
                "пользователем с id = {} .", bookerId);
        if (bookingDto.getStart().isAfter(bookingDto.getEnd())
                || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new BadRequestException("Начало аренды не может быть позже или равно сдачи вещи");
        }
        return bookingService.addBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                             @PathVariable Long bookingId,
                                             @RequestParam boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту: /bookings/{bookingId}?approved={approved} с ответом " +
                "на потверждение бронирования вещи владельцем.");

        if (approved) {
            log.info("Бронирование подтверждено.");
        } else {
            log.info("Бронирование отклонено.");
        }
        return bookingService.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@RequestHeader(REQUEST_HEADER) Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Получен GET-запрос к эндпоинту: /bookings/{bookingId} на получение информации " +
                "о бронировании вещи пользователем с id = {} .", userId);
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getUserBookings(
            @RequestHeader(REQUEST_HEADER) Long bookerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size) {
        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings на получение информации о бронирование" +
                " пользователя с id = {}", bookerId);

        return bookingService.getUserBookings(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader(REQUEST_HEADER) Long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "20") Integer size) {
        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings/owner на получение информации о бронирование" +
                " собственника с id = {}", ownerId);
        return bookingService.getOwnerBookings(ownerId, bookingState, from, size);
    }
}
