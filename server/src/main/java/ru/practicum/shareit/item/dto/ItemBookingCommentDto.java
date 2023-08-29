package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemBookingCommentDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingBookerDto lastBooking;
    BookingBookerDto nextBooking;
    List<CommentResponseDto> comments;
}
