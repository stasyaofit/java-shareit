package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
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
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;
    private User owner;
    private User booker;
    private Booking booking;
    private final Pageable page = PageRequest.of(0, 10);
    private final LocalDateTime now =
            LocalDateTime.of(2023, Month.AUGUST, 23, 20, 0, 0);

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("owner")
                .email("owner@test.ru")
                .build();

        userRepository.save(owner);

        booker = User.builder()
                .name("booker")
                .email("booker@test.ru")
                .build();

        userRepository.save(booker);

        Item item = Item.builder()
                .name("Item")
                .description("description")
                .owner(owner)
                .available(true)
                .request(null)
                .build();

        itemRepository.save(item);

        booking = Booking.builder()
                .start(LocalDateTime.of(2023, Month.SEPTEMBER, 4, 15, 16, 1))
                .end(LocalDateTime.of(2023, Month.OCTOBER, 4, 15, 16, 1))
                .status(Status.WAITING)
                .item(item)
                .booker(booker)
                .build();

        bookingRepository.save(booking);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void findById() {
        Optional<Booking> actual = bookingRepository.findById(booking.getId());
        actual.ifPresent(value -> assertEquals(value, booking));
    }

    @Test
    void findByBookerIdOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository.findByBookerIdOrderByStartDesc(booker.getId(), page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByBookerIdAndStartAfterOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByBookerIdAndStartAfterOrderByStartDesc(booker.getId(), now, page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByBookerIdAndEndBeforeOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByBookerIdAndEndBeforeOrderByStartDesc(booker.getId(), now.plusYears(1), page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(booker.getId(),
                        now.plusMonths(2), now.plusMonths(1), page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByBookerIdAndStatusEquals() {
        List<Booking> actualBookings = bookingRepository
                .findByBookerIdAndStatusEquals(booker.getId(), Status.WAITING, page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByItem_OwnerOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByItem_OwnerOrderByStartDesc(owner, page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(owner, now.plusMonths(2),
                        now.plusMonths(1), page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByItem_OwnerAndEndBeforeOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByItem_OwnerAndEndBeforeOrderByStartDesc(owner, now.plusMonths(4), page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByItem_OwnerAndStartAfterOrderByStartDesc() {
        List<Booking> actualBookings = bookingRepository
                .findByItem_OwnerAndStartAfterOrderByStartDesc(owner, now, page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }

    @Test
    void findByItem_OwnerAndStatusEquals() {
        List<Booking> actualBookings = bookingRepository
                .findByItem_OwnerAndStatusEquals(owner, Status.WAITING, page);
        Booking actualBooking = actualBookings.get(0);

        assertEquals(actualBooking, booking);
        assertThat(actualBookings.size(), is(1));
    }
}