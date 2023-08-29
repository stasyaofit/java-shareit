package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        item = new Item();

        User owner = User.builder()
                .name("owner")
                .email("owner@test.ru")
                .build();

        userRepository.save(owner);

        User requester = User.builder()
                .name("requester")
                .email("requester@test.ru")
                .build();

        userRepository.save(requester);

        item = Item.builder()
                .name("Item")
                .description("description")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        itemRepository.save(item);

        comment = Comment.builder()
                .text("comment")
                .item(item)
                .author(requester)
                .created(LocalDateTime.of(2023, Month.AUGUST, 22, 15, 16, 1))
                .build();

        commentRepository.save(comment);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findById() {
        Optional<Comment> actual = commentRepository.findById(comment.getId());
        actual.ifPresent(value -> assertEquals(value, comment));
    }

    @Test
    void findAllByItemId() {
        List<Comment> actualComments = commentRepository.findByItem_Id(item.getId());
        Comment actualComment = actualComments.get(0);

        assertEquals(actualComment, comment);
        assertThat(actualComments.size(), is(1));
    }
}
