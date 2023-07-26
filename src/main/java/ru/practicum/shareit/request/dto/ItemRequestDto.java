package ru.practicum.shareit.request.dto;

import lombok.Getter;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter
public class ItemRequestDto {
    Long id;
    @NotEmpty(message = "Описание не может быть пустым")
    String description;
    User requestor;
    @FutureOrPresent(message = "Время создания не может быть в прошлом")
    LocalDateTime created;
}
