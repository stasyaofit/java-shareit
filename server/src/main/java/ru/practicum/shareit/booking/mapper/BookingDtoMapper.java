package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface BookingDtoMapper {
    @Mapping(target = "id", ignore = true)
    Booking mapToBooking(BookingRequestDto dto, Item item, User booker, Status status);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingBookerDto toBookingBookerDto(Booking booking);

    BookingResponseDto mapToBookingResponseDto(Booking booking);

    List<BookingResponseDto> mapToBookingResponseDtoList(List<Booking> bookings);
}
