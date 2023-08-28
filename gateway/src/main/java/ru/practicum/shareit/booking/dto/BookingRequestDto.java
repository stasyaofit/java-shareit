package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
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
