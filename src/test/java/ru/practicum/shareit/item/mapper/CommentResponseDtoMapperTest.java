package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentResponseDtoMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class CommentResponseDtoMapperTest {

    @Autowired
    private CommentResponseDtoMapper mapper;
    private Item item;
    private User user;
    private LocalDateTime created;

    @BeforeEach
    void setup() {
        user = User.builder()
                .id(1L)
                .email("user@mail.com")
                .name("user").build();
        item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(user)
                .request(ItemRequest.builder().id(1L).build())
                .build();
        created = LocalDateTime.now();
    }

    @Test
    void toCommentResponseDto() {
        //given
        Comment comment = Comment.builder()
                .id(1L)
                .author(user)
                .item(item)
                .text("comment")
                .created(created)
                .build();
        //when
        CommentResponseDto dto = mapper.toCommentResponseDto(comment);
        //then
        assertEquals(comment.getAuthor().getName(), dto.getAuthorName());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getCreated(), dto.getCreated());
    }

    @Test
    void toComment() {
        CommentDto dto = CommentDto.builder()
                .text("comment")
                .build();
        Comment comment = mapper.toComment(dto, user, item, created);

        assertEquals(comment.getItem().getDescription(), item.getDescription());
        assertEquals(comment.getAuthor().getName(), user.getName());
        assertEquals(comment.getCreated(), created);
        assertNull(comment.getId());
    }
}
