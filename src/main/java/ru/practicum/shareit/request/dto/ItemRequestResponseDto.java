package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;

@Data
public class ItemRequestResponseDto {
    Long id;
    String description;
    User requester;
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    LocalDateTime created;
    List<ItemForRequestDto> items;
}
