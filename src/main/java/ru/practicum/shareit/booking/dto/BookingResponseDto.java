package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.user.dto.ShortUserDto;

import java.time.LocalDateTime;

import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingResponseDto {
    Long id;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime start;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime end;
    Status status;
    ShortUserDto booker;
    ShortItemDto item;
}




