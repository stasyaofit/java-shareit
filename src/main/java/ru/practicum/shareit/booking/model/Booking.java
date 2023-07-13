package ru.practicum.shareit.booking.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Booking {
    Long id;
    @FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    LocalDateTime start;
    @Future(message = "Окончание бронирования не может быть в прошлом")
    LocalDateTime end;
    @NotNull
    Item item;
    @NotNull
    User booker;
    @NotNull
    Status status;
}
