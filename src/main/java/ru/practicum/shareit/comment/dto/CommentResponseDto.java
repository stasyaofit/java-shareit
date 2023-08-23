package ru.practicum.shareit.comment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponseDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;
}
