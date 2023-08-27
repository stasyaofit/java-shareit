package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto addBooking(BookingRequestDto bookingDto, Long bookerId);

    BookingResponseDto approveBooking(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getUserBookings(Long bookerId, BookingState state, Integer from, Integer size);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, Integer from, Integer size);
}
