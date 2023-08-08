package ru.practicum.shareit.request.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ItemRequestShortDto {
    @NotEmpty(message = "Описание запроса не может быть пустым")
    private String description;
}
