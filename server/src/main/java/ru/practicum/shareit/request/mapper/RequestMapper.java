package ru.practicum.shareit.request.mapper;

import lombok.Generated;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Generated
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface RequestMapper {
    ItemRequestResponseDto mapToRequestResponseDto(ItemRequest request, List<ItemForRequestDto> items);

    @Mapping(target = "id", ignore = true)
    ItemRequest fromShortDto(ItemRequestShortDto dto, User requester, List<Item> items, LocalDateTime created);
}
