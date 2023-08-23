package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.ShortUserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {

    @InjectMocks
    private final BookingServiceImpl bookingService;

    @MockBean
    private final BookingRepository bookingRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRepository itemRepository;

    private static final LocalDateTime DEFAULT_START_DATE = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
    private static final LocalDateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusDays(1);

    private User user1, user2;
    private Item item1, item2;

    private BookingRequestDto booking1Dto;

    private Booking booking1ByUser2;
    private BookingResponseDto response1Dto;

    @BeforeEach
    void setup() {
        setupUsersAndItems();
        setupEntityDtos();
    }

    @Test
    @DisplayName("Добавление валидного бронирования успешно")
    void addBooking_whenInputOk_thenOk() {
        //given
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(bookingRepository.save(any())).thenAnswer(
                invocationOnMock -> {
                    Booking booking = invocationOnMock.getArgument(0, Booking.class);
                    booking.setId(1L);
                    return booking;
                }
        );
        //when
        BookingResponseDto responseDtoResult = bookingService.addBooking(booking1Dto, 2L);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(DEFAULT_START_DATE, responseDtoResult.getStart());
        assertEquals(DEFAULT_END_DATE, responseDtoResult.getEnd());
        assertEquals("item1", responseDtoResult.getItem().getName());
        assertEquals(2L, responseDtoResult.getBooker().getId());
    }

    @Test
    @DisplayName("Добавление бронирования не существующим пользователем выбросит исключение")
    void addBooking_whenBookerNotFound_thenNotFoundException() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        //when
        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> bookingService.addBooking(booking1Dto, 2L)
        );
        //then
        assertEquals("Пользователь с id = " + 2L + " не найден.", e.getMessage());
        verify(userRepository, only()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Добавление бронирования для не существующей вещи выбросит исключение")
    void addBooking_whenItemNotFound_thenNotFoundException() {
        //given
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        //when
        ItemNotFoundException e = assertThrows(ItemNotFoundException.class,
                () -> bookingService.addBooking(booking1Dto, 2L)
        );
        //then
        assertEquals("Вещь с id = " + 1L + " не найдена.", e.getMessage());
        verify(userRepository, only()).findById(2L);
        verify(itemRepository, only()).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Добавление бронирования вещи её собственником выбросит исключение")
    void addBooking_whenBookerIsOwner_thenNotFoundException() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item2));
        //when
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(booking1Dto, 2L)
        );
        //then
        assertEquals("Владелец вещи не может бронировать свои вещи.", e.getMessage());
        verify(userRepository, only()).findById(anyLong());
        verify(itemRepository, only()).findById(anyLong());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Добавление бронирования для недоступной вещи выбросит исключение")
    void addBooking_whenItemNotAvailable_thenBadRequestException() {
        //given
        item1.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        //when
        BadRequestException e = assertThrows(BadRequestException.class,
                () -> bookingService.addBooking(booking1Dto, 2L)
        );
        //then
        assertEquals("Вещь недоступна.", e.getMessage());
        verify(userRepository, only()).findById(2L);
        verify(itemRepository, only()).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Потдверждение/отклонение бронирования владельцем вещи")
    void approve_whenInputOk_thenOk(boolean approvalState) {
        //given
        Map<Boolean, Status> statuses = Map.of(true, Status.APPROVED, false, Status.REJECTED);
        Booking booking = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        //when
        BookingResponseDto responseDto = bookingService.approveBooking(1L, 1L, approvalState);
        //then
        assertEquals(statuses.get(approvalState), responseDto.getStatus());
        verify(bookingRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Потдверждение/отклонение бронирования вещи не существующим пользоватем выбросит исключение")
    void approve_whenBookingNotFound_thenNotFoundException() {
        //given
        Long bookingId = 1L;
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        //when
        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> bookingService.approveBooking(1L, bookingId, true)
        );
        //then
        assertEquals("Пользователь с id = " + 1L + " не найден.", e.getMessage());
        verify(bookingRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Потдверждение/отклонение бронирования вещи не её владельцем выбросит исключение")
    void approve_whenBadOwnerId_thenNotFoundException() {
        //given
        Long bookingId = 1L;
        Long ownerId = 1L;
        User user3 = User.builder().id(3L).name("user3").email("user3@mail.com").build();
        item1.setOwner(user3);
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user3));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(ownerId, bookingId, true)
        );
        //then
        assertEquals("Пользователь не является владельцем вещи.", e.getMessage());
        verify(bookingRepository, only()).findById(anyLong());
    }

    @Test
    @DisplayName("Бронирования уже забронированной вещи невуозможн, выбросит исключение")
    void approve_whenBookingStatusAlreadyApproved_thenBadRequestException() {
        //given
        Long bookingId = 1L;
        Long ownerId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.APPROVED)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        BadRequestException bre = assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(ownerId, bookingId, true)
        );
        //then
        assertEquals("Бронь с id = " + bookingId + " не ожидает подтверждения", bre.getMessage());
        verify(bookingRepository, only()).findById(anyLong());
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L})
    @DisplayName("Успешное получение информации о бронировании вещи её владельцем или арендатором")
    void getBookingById_thenInputOk_thenOk(long relatedUserId) {
        //given
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        BookingResponseDto responseDtoResult = bookingService.getBookingById(relatedUserId, bookingId);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(DEFAULT_START_DATE, responseDtoResult.getStart());
        assertEquals(DEFAULT_END_DATE, responseDtoResult.getEnd());
        assertEquals("item1", responseDtoResult.getItem().getName());
        assertEquals(user2.getId(), responseDtoResult.getBooker().getId());
    }

    @Test
    @DisplayName("Получение информации о бронировании вещи не относящегося к ней пользователя невозможно, выбросит исключение")
    void getBookingById_thenUserIdNotRelated_thenNotFoundException() {
        //given
        Long bookingId = 1L;
        User user3 = User.builder().id(3L).name("user3").email("user3@mail.com").build();
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user3));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        //when
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(user3.getId(), bookingId)
        );
        //then
        assertEquals("Бронь с id = " + bookingId +
                " для пользователя с id = " + user3.getId() + " не найдена", e.getMessage());
        verify(bookingRepository, only()).findById(anyLong());
    }

    @Test
    @DisplayName("Получение информации о всех бронированиях арендатором")
    void getListByBooker_thenStatusAll_thenInvokes_FindByBookerIdOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdOrderByStartDesc(anyLong(), any())).thenReturn(List.of(booking1ByUser2));

        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, BookingState.ALL, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, only()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех будующих бронированиях арендатором")
    void getListByBooker_thenStatusFuture_thenInvokes_findByBookerIdAndStartIsAfterOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, BookingState.FUTURE, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, only()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех завершившихся бронированиях арендатором")
    void getListByBooker_thenStatusPast_thenInvokes_findByBookerIdAndEndIsBeforeOrderByEndDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, BookingState.PAST, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, only()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех текущих бронированиях арендатором")
    void getListByBooker_thenStatusCurrent_thenInvokes_findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByEndDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, BookingState.CURRENT, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, only()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "rejected", "canceled"})
    @DisplayName("Получение информации о всех бронированиях арендатором с определённым статусом")
    void getListByBooker_thenStatuses_thenInvokes_findByBookerIdAndStatusOrderByStartDesc(String str) {
        //given
        Optional<BookingState> status = BookingState.toState(str);
        if (status.isEmpty()) {
            verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
            return;
        }
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdAndStatusEquals(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, status.get(), 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, only()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Получение информации о бронированиях для несуществующего пользователя выбросит исключени")
    void getListByBooker_thenUserNotExists_thenThrowNotFound() {
        //given
        Long userId = 2L;
        //when
        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> bookingService.getUserBookings(userId, BookingState.WAITING, 0, 20)
        );
        assertEquals("Пользователь с id = " + userId + " не найден.", e.getMessage());
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                anyLong(), any(), any(), any());
        verify(bookingRepository, never()).findByBookerIdAndStatusEquals(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех ожидающих подтверждения бронированиях арендатором")
    void getListByBooker_thenInputOk_thenReturnDtoList() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByBookerIdAndStatusEquals(anyLong(), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getUserBookings(2L, BookingState.WAITING, 0, 20);
        //then
        assertEquals(response1Dto.getStart(), list.get(0).getStart());
        assertEquals(response1Dto.getEnd(), list.get(0).getEnd());
        assertEquals(response1Dto.getId(), list.get(0).getId());
        assertEquals(response1Dto.getStatus(), list.get(0).getStatus());
        assertEquals(response1Dto.getItem().getName(), list.get(0).getItem().getName());
        assertEquals(response1Dto.getBooker().getId(), list.get(0).getBooker().getId());
    }

    @Test
    @DisplayName("Получение информации о всех бронированиях владельцем")
    void getListByOwner_thenStatusAll_thenInvokes_FindByItem_OwnerIdOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerOrderByStartDesc(any(User.class), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, BookingState.ALL, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, only()).findByItem_OwnerOrderByStartDesc(any(User.class), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(
                any(User.class), any(), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех будующих бронированиях владельцем")
    void getListByOwner_thenStatusFuture_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, BookingState.FUTURE, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByItem_OwnerOrderByStartDesc(any(User.class), any());
        verify(bookingRepository, only()).findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(
                any(User.class), any(), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех завершённых бронированиях владельцем")
    void getListByOwner_thenStatusPast_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, BookingState.PAST, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByItem_OwnerOrderByStartDesc(any(User.class), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, only()).findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(
                any(User.class), any(), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех текущих бронированиях владельцем")
    void getListByOwner_thenStatusCurrent_thenInvokes_FindByItem_OwnerIdAndStartIsAfterOrderByStartDesc() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(any(User.class), any(),
                any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, BookingState.CURRENT, 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByItem_OwnerOrderByStartDesc(any(User.class), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, only()).findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(
                any(User.class), any(), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "rejected", "canceled"})
    @DisplayName("Получение информации о всех бронированиях владельцем с определённым статусом")
    void getListByOwner_thenStatuses_thenInvokes_findByItem_OwnerIdAndStatusOrderByStartDesc(String str) {
        //given
        Optional<BookingState> status = BookingState.toState(str);
        if (status.isEmpty()) {
            verify(bookingRepository, never()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
            return;
        }
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerAndStatusEquals(any(User.class), any(), any()))
                .thenReturn(List.of(booking1ByUser2));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, status.get(), 0, 20);
        assertEquals(1, list.size());
        verify(bookingRepository, never()).findByItem_OwnerOrderByStartDesc(any(User.class), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartAfterOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndEndBeforeOrderByStartDesc(any(User.class), any(), any());
        verify(bookingRepository, never()).findByItem_OwnerAndStartBeforeAndEndAfterOrderByStartDesc(
                any(User.class), any(), any(), any());
        verify(bookingRepository, only()).findByItem_OwnerAndStatusEquals(any(User.class), any(), any());
    }

    @Test
    @DisplayName("Получение информации о всех ожидающих подтверждения бронированиях владельцем")
    void getListByOwner_thenInputOk_thenReturnDtoList() {
        //given
        Long bookingId = 1L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2));
        when(bookingRepository.findByItem_OwnerAndStatusEquals(any(User.class), any(), any()))
                .thenReturn(List.of(booking));
        //when
        List<BookingResponseDto> list = bookingService.getOwnerBookings(2L, BookingState.WAITING, 0, 20);
        //then
        assertEquals(response1Dto.getStart(), list.get(0).getStart());
        assertEquals(response1Dto.getEnd(), list.get(0).getEnd());
        assertEquals(response1Dto.getId(), list.get(0).getId());
        assertEquals(response1Dto.getStatus(), list.get(0).getStatus());
        assertEquals(response1Dto.getItem().getName(), list.get(0).getItem().getName());
        assertEquals(response1Dto.getBooker().getId(), list.get(0).getBooker().getId());
    }

    private void setupUsersAndItems() {
        user1 = User.builder().id(1L).name("user1").email("user1@mail.com").build();
        user2 = User.builder().id(2L).name("user2").email("user2@mail.com").build();
        item1 = Item.builder().id(1L).owner(user1).name("item1").description("description1").available(true).build();
        item2 = Item.builder().id(2L).owner(user2).name("item2").description("description2").available(true).build();
    }

    private void setupEntityDtos() {
        booking1Dto = BookingRequestDto.builder()
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .itemId(1L)
                .build();
        response1Dto = BookingResponseDto.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .booker(ShortUserDto.builder().id(2L).build())
                .item(ShortItemDto.builder().id(1L).name("item1").build())
                .build();
        booking1ByUser2 = Booking.builder()
                .id(1L)
                .start(DEFAULT_START_DATE)
                .end(DEFAULT_END_DATE)
                .status(Status.WAITING)
                .item(item1)
                .booker(user2)
                .build();
    }
}
