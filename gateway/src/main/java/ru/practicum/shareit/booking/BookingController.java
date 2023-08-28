package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnsupportedStatusException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader(REQUEST_HEADER) Long bookerId,
                                              @RequestParam(defaultValue = "ALL") String state,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                              @RequestParam(defaultValue = "10") @Positive Integer size) {

        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings на получение информации о бронирование" +
                " пользователя с id = {}", bookerId);
        return bookingClient.getUserBookings(bookerId, bookingState, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(REQUEST_HEADER) Long ownerId,
                                                     @RequestParam(defaultValue = "ALL") String state,
                                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                     @RequestParam(defaultValue = "20") @Positive Integer size) {

        BookingState bookingState = BookingState.toState(state).orElseThrow(
                () -> new UnsupportedStatusException("Unknown state: " + state));
        log.info("Получен GET-запрос к эндпоинту: /bookings/owner на получение информации о бронирование" +
                " собственника с id = {}", ownerId);
        return bookingClient.getOwnerBookings(ownerId, bookingState, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(REQUEST_HEADER) Long bookerId,
                                           @RequestBody @Valid BookingRequestDto requestDto) {
        log.info("Получен POST-запрос к эндпоинту: /bookings на бронирование вещи " +
                "пользователем с id = {} .", bookerId);
        return bookingClient.addBooking(requestDto, bookerId);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(REQUEST_HEADER) Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Получен GET-запрос к эндпоинту: /bookings/{bookingId} на получение информации " +
                "о бронировании вещи пользователем с id = {} .", userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(value = REQUEST_HEADER) Long ownerId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос к эндпоинту: /bookings/{bookingId}?approved={approved} с ответом " +
                "на потверждение бронирования вещи владельцем.");
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }

}