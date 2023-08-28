package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User owner;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        Item item;
        itemRequest = new ItemRequest();

        owner = User.builder()
                .name("owner")
                .email("owner@test.ru")
                .build();

        userRepository.save(owner);

        requester = User.builder()
                .name("requester")
                .email("requestert@test.ru")
                .build();

        userRepository.save(requester);

        item = Item.builder()
                .name("Item")
                .description("description")
                .owner(owner)
                .available(true)
                .request(null)
                .build();

        itemRepository.save(item);

        itemRequest = ItemRequest.builder()
                .description("Request description")
                .requester(requester)
                .created(LocalDateTime.of(2023, Month.AUGUST, 25, 10, 0, 0))
                .build();

        itemRequestRepository.save(itemRequest);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc() {
        List<ItemRequest> actualRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requester.getId());
        ItemRequest actualRequest = actualRequests.get(0);

        assertEquals(itemRequest, actualRequest);
        assertThat(actualRequests.size(), is(1));
    }

    @Test
    void findAllByRequesterIdIsNot() {
        Pageable page = PageRequest.of(0, 10);
        List<ItemRequest> actualRequests = itemRequestRepository.findAllByRequesterIdNot(owner.getId(), page);
        ItemRequest actualRequest = actualRequests.get(0);

        assertEquals(itemRequest, actualRequest);
        assertThat(actualRequests.size(), is(1));
    }
}
