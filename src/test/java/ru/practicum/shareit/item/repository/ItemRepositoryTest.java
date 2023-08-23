package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    private ItemRepository repository;

    @Autowired
    private UserRepository userRepository;

    private Item item;
    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("user")
                .email("user@mail.ru")
                .build();

        userRepository.save(owner);

        item = Item.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .owner(owner)
                .request(null)
                .build();

        repository.save(item);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void findAllItemsByUserId() {
        Pageable page = PageRequest.of(0, 10);
        List<Item> actualItems = repository.findItemByOwner_IdIs(owner.getId(), page);
        Item actual = actualItems.get(0);

        assertEquals(actual, item);
        assertThat(actualItems.size(), is(1));
    }

    @Test
    void findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue() {
        Pageable page = PageRequest.of(0, 10);
        String text = "desCRIPtion";

        List<Item> actualItems = repository.findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(text, text, page);
        Item actual = actualItems.get(0);

        assertEquals(actual, item);
        assertThat(actualItems.size(), is(1));
    }
}
