package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplWithDBAddBookingTest {

    private final BookingServiceImpl bookingService;
    private final JdbcTemplate jdbcTemplate;
    private static final Long DEFAULT_ITEM_ID = 1L;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);
    private final BookingRequestDto initial = BookingRequestDto.builder()
            .itemId(DEFAULT_ITEM_ID)
            .start(DEFAULT_START_DATE)
            .end(DEFAULT_END_DATE)
            .build();

    @BeforeEach
    void reinitialiseBookings() {
        jdbcTemplate.update("DELETE FROM bookings");
    }

    @Sql(scripts = "/booking.sql")
    @Test
    void addBookingAndGetById() {
        BookingResponseDto expected = BookingResponseDto.builder()
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .build();

        //when
        SqlRowSet set = jdbcTemplate.queryForRowSet("SELECT user_id, name from users");
        int count = 0;
        long bookerId = 0;
        long ownerId = 0;
        while (set.next()) {
            if (Objects.equals(set.getString("name"), "booker")) bookerId = set.getLong("user_id");
            if (Objects.equals(set.getString("name"), "owner")) ownerId = set.getLong("user_id");
            count++;
        }
        if (count != 2 && bookerId == 0 && ownerId == 0) fail();
        BookingResponseDto booking = bookingService.addBooking(initial, bookerId);
        long bookingId = booking.getId();
        BookingResponseDto actualByOwner = bookingService.getBookingById(ownerId, bookingId);
        BookingResponseDto actualByBooker = bookingService.getBookingById(bookerId, bookingId);

        //then
        assertThat(actualByOwner)
                .isNotNull()
                .hasNoNullFieldsOrProperties();
        assertEquals(expected.getStart(), actualByOwner.getStart());
        assertEquals(expected.getEnd(), actualByOwner.getEnd());
        assertEquals(ownerId, actualByOwner.getItem().getId());
        assertEquals("item", actualByOwner.getItem().getName());
        assertEquals(bookerId, actualByOwner.getBooker().getId());
        assertEquals(actualByOwner, actualByBooker);
    }
}