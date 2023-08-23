package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.user.dto.ShortUserDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;

@JsonTest
class BookingResponseDtoTest {

    @Autowired
    private JacksonTester<BookingResponseDto> jacksonTester;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.now();

    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @Test
    void serializeJsonTest() throws IOException {
        BookingResponseDto dto = BookingResponseDto.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .booker(new ShortUserDto(1L))
                .item(new ShortItemDto(1L, "item"))
                .build();

        JsonContent<BookingResponseDto> jsonContent = jacksonTester.write(dto);

        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.start").isEqualTo(dto.getStart().format(dtf));
        assertThat(jsonContent).extractingJsonPathStringValue("$.end").isEqualTo(dto.getEnd().format(dtf));
        assertThat(jsonContent).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.item.name").isEqualTo(dto.getItem().getName());
        assertThat(jsonContent).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.status").isEqualTo(dto.getStatus().toString());
    }
}
