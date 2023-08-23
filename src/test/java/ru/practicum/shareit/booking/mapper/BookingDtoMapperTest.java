package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class BookingDtoMapperTest {

    private final LocalDateTime start = LocalDateTime.MIN;
    private final LocalDateTime end = LocalDateTime.MAX;

    private final User booker = User.builder().id(1L).name("owner").email("owner@mail.com").build();
    private final BookingRequestDto bookingDto = BookingRequestDto.builder()
            .start(start).end(end)
            .itemId(1L)
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .owner(booker)
            .name("item")
            .description("description")
            .available(true)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(start).end(end)
            .item(item)
            .booker(booker)
            .build();

    @Autowired
    private BookingDtoMapper mapper;

    @Test
    void toBookingResponseDto() {
        //when
        BookingResponseDto bookingResponseDto = mapper.mapToBookingResponseDto(booking);
        //then
        assertNull(mapper.mapToBookingResponseDto(null));
        assertNotNull(bookingResponseDto);
        assertEquals(start, bookingResponseDto.getStart());
        assertEquals(end, bookingResponseDto.getEnd());
        assertEquals(1L, bookingResponseDto.getItem().getId());
        assertEquals(1L, bookingResponseDto.getBooker().getId());
    }

    @Test
    void toBooking_whenNullArgs_thenNull() {
        //when
        Booking bookingNoBooker = mapper.mapToBooking(bookingDto, item, null, null);
        Booking bookingNoItem = mapper.mapToBooking(bookingDto, null, booker, null);
        //then
        assertNotNull(bookingNoBooker);
        assertNotNull(bookingNoItem);
        assertEquals(start, bookingNoBooker.getStart());
        assertEquals(start, bookingNoItem.getStart());
        assertEquals(end, bookingNoBooker.getEnd());
        assertEquals(end, bookingNoItem.getEnd());
        assertNull(bookingNoBooker.getBooker());
        assertNull(bookingNoBooker.getStatus());
        assertNull(bookingNoItem.getItem());
        assertNull(bookingNoItem.getStatus());
    }

    @Test
    void toBooking() {
        //when
        Booking booking = mapper.mapToBooking(bookingDto, item, booker, Status.WAITING);
        //then
        assertNotNull(booking);
        assertEquals(start, booking.getStart());
        assertEquals(end, booking.getEnd());
        assertEquals(1L, bookingDto.getItemId());
        assertEquals(Status.WAITING, booking.getStatus());
    }

    @Test
    void toUserDtoList() {
        List<Booking> bookings = List.of(booking);
        List<BookingResponseDto> bookingResponseDtoList = mapper.mapToBookingResponseDtoList(bookings);

        assertEquals(1, bookingResponseDtoList.size());
        assertEquals(1L, bookingResponseDtoList.get(0).getId());
        assertEquals(start, bookingResponseDtoList.get(0).getStart());
        assertEquals(end, bookingResponseDtoList.get(0).getEnd());
        assertEquals(1L, bookingResponseDtoList.get(0).getItem().getId());
        assertEquals(1L, bookingResponseDtoList.get(0).getBooker().getId());
    }
}

