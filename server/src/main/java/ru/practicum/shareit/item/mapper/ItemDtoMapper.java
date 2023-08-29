package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface ItemDtoMapper {
    @Mapping(target = "requestId", source = "item.request.id")
    ItemDto toItemDto(Item item);

    Item toItem(ItemDto itemDto);

    @Mapping(target = "requestId", source = "item.request.id")
    @Mapping(target = "ownerId", source = "item.owner.id")
    ItemForRequestDto mapItemToItemForRequest(Item item);

    List<ItemDto> toItemDtoList(List<Item> items);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "dtoLast")
    @Mapping(target = "nextBooking", source = "dtoNext")
    ItemBookingCommentDto toItemWithBookings(Item item, BookingBookerDto dtoLast, BookingBookerDto dtoNext);
}
