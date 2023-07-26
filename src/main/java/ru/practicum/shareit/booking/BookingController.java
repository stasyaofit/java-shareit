package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@RestController
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponseDto addBooking(@RequestHeader(REQUEST_HEADER) Long bookerId,
                                         @RequestBody @Valid BookingRequestDto bookingDto) {
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
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings на получение информации о бронирование" +
                " пользователя с id = {}", bookerId);

        return bookingService.getUserBookings(bookerId, bookingState);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(
            @RequestHeader(REQUEST_HEADER) Long ownerId,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings/owner на получение информации о бронирование" +
                " собственника с id = {}", ownerId);
        return bookingService.getOwnerBookings(ownerId, bookingState);
    }
}
