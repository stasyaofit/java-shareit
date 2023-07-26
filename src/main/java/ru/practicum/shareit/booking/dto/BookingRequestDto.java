package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDto {
    Long itemId;
    @FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    @NotNull
    LocalDateTime start;
    @FutureOrPresent(message = "Окончание бронирования не может быть в прошлом")
    @NotNull
    LocalDateTime end;
}
