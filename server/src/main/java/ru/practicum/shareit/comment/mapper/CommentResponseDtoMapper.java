package ru.practicum.shareit.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface CommentResponseDtoMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentResponseDto toCommentResponseDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    Comment toComment(CommentDto commentDto, User author, Item item, LocalDateTime created);
}
