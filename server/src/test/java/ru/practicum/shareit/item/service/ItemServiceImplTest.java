package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repositoty.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.OperationAccessException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {

    @InjectMocks
    private final ItemServiceImpl itemService;

    @MockBean
    private final ItemRepository itemRepository;

    @MockBean
    private final BookingRepository bookingRepository;

    @MockBean
    private final CommentRepository commentRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRequestRepository requestRepository;

    private User user1, user2;

    private final Long user1Id = 1L;
    private final Long user2Id = 2L;
    private final Long item1Id = 1L;
    private Item item1;

    private ItemDto item1Dto;

    private Comment comment1;

    private CommentDto comment1Dto;

    private ItemRequest request1byUser2;
    private LocalDateTime currentTime;

    private Booking booking1Next, booking1Last;

    private static final String ITEM_NAME = "item1";
    private static final String ITEM_DESCRIPTION = "description1";
    private static final String COMMENT = "comment1";

    @BeforeEach
    void setup() {
        setupUsersAndItemsAndDto();
    }

    @Nested
    class AddItemTest {
        @Nested
        class WhenOk {
            @Test
            @DisplayName("Добавление валидной вещи успешно")
            void whenInputOkNoRequestId() {
                //given
                when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
                when(itemRepository.save(any())).thenAnswer(
                        invocationOnMock -> {
                            Item i = invocationOnMock.getArgument(0, Item.class);
                            i.setId(item1Id);
                            return i;
                        }
                );
                //when
                ItemDto responseDtoResult = itemService.saveItem(item1Dto, item1Id);
                //then
                checkItemDtoBaseParam(responseDtoResult);
                verify(requestRepository, never()).findById(anyLong());
            }

            @Test
            @DisplayName("Добавление валидной вещи в ответ на запрос успешно")
            void addItem_whenInputOkWithRequestId() {
                //given
                when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
                when(requestRepository.findById(1L)).thenReturn(Optional.of(request1byUser2));
                when(itemRepository.save(any())).thenAnswer(
                        invocationOnMock -> {
                            Item i = invocationOnMock.getArgument(0, Item.class);
                            i.setId(item1Id);
                            return i;
                        }
                );
                //when
                ItemDto responseDtoResult = itemService.saveItem(item1Dto, item1Id, request1byUser2.getId());
                //then
                checkItemDtoBaseParam(responseDtoResult);
                assertEquals(request1byUser2.getId(), responseDtoResult.getRequestId());
                verify(requestRepository, only()).findById(anyLong());
            }
        }

        @Nested
        class WhenThrows {
            @Test
            @DisplayName("Добавление вещи не существующим пользователем выбросит исключение")
            void addItem_whenUserNotFound_thenNotFoundException() {
                //given
                when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
                //when
                UserNotFoundException e = assertThrows(UserNotFoundException.class,
                        () -> itemService.saveItem(item1Dto, user1Id)
                );
                //then
                assertEquals("Пользователь с id = " + user1Id + " не найден.", e.getMessage());
                verify(userRepository, only()).findById(anyLong());
                verify(requestRepository, never()).findById(anyLong());
            }
        }
    }

    @Nested
    class UpdateItemTest {
        @Nested
        class WhenOk {
            @Test
            @DisplayName("Частичное обновление вещи её собственником успешно")
            void patch_whenInputOk() {
                //given
                ItemDto updateDtoOnlyDescription = ItemDto.builder().description("updated1").build();
                when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
                when(itemRepository.save(any(Item.class))).thenReturn(item1);
                //when
                ItemDto responseDtoResult = itemService.updateItem(updateDtoOnlyDescription, item1Id, user1Id);
                //then
                assertThat(responseDtoResult).isNotNull();
                assertEquals(item1Id, responseDtoResult.getId());
                assertEquals(ITEM_NAME, responseDtoResult.getName());
                assertEquals("updated1", responseDtoResult.getDescription());
                assertTrue(responseDtoResult.getAvailable());
                verify(requestRepository, never()).findById(anyLong());
            }
        }

        @Nested
        class WhenThrows {
            @Test
            @DisplayName("Обновление вещи возможно только для владельца, иначе выбросит исключение")
            void patch_whenBadOwnerId_thenNotFoundException() {
                //given
                ItemDto updateDtoOnlyDescription = ItemDto.builder().description("updated1").build();
                when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
                //when
                OperationAccessException e = assertThrows(OperationAccessException.class,
                        () -> itemService.updateItem(updateDtoOnlyDescription, item1Id, user2Id)
                );
                //then
                assertEquals("Пользователь с id = " + user2Id + " не является собственником вещи.", e.getMessage());
                verify(itemRepository, never()).save(any());
            }
        }
    }

    @Nested
    class GetOneTest {
        @Nested
        class WhenOk {
            @Test
            @DisplayName("Получение вещи по id её собственником без бронирований и комментариев")
            void getByOwnerById_thenInputOkAndNoAdditionalData_thenOkNullBookingsCommentsEmpty() {
                //given
                when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
                when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
                when(bookingRepository.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                        anyLong(),
                        any(),
                        any())
                ).thenReturn(null);
                when(bookingRepository.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any())
                ).thenReturn(null);
                when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of());
                //when
                ItemBookingCommentDto responseDtoResult = itemService.getItemById(user1Id, item1Id);
                //then

                checkItemBookingCommentDtoBaseParam(responseDtoResult);
                assertNotNull(responseDtoResult.getComments());
                assertTrue(responseDtoResult.getComments().isEmpty());
                assertNull(responseDtoResult.getLastBooking());
                assertNull(responseDtoResult.getNextBooking());
                verify(bookingRepository).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(bookingRepository).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(commentRepository).findByItem_Id(anyLong());
            }

            @Test
            @DisplayName("Получение вещи по id её собственником с заполненной информацией о бронированиях и комментариях")
            void getByOwnerById_thenInputOk_thenOkWithAdditionalData() {
                //given
                when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
                when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
                when(bookingRepository.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                        anyLong(),
                        any(),
                        any())
                ).thenReturn(booking1Last);
                when(bookingRepository.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any())
                ).thenReturn(booking1Next);
                when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(comment1));
                //when
                ItemBookingCommentDto responseDtoResult = itemService.getItemById(user1Id, item1Id);
                //then
                checkItemDtoWithComment(responseDtoResult);

                assertEquals(booking1Last.getId(), responseDtoResult.getLastBooking().getId());
                assertEquals(booking1Next.getId(), responseDtoResult.getNextBooking().getId());

                verify(bookingRepository).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(bookingRepository).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(commentRepository).findByItem_Id(anyLong());
            }

            @Test
            @DisplayName("Получение вещи по id не владельцем возможно только с заполненной информацией о комментариях")
            void getByOwnerById_thenNotOwnerUser_thenOkWithCommentsOnlyData() {
                //given
                when(itemRepository.findById(item1Id)).thenReturn(Optional.of(item1));
                when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
                when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(comment1));
                //when
                ItemBookingCommentDto responseDtoResult = itemService.getItemById(user2Id, item1Id);
                //then
                checkItemDtoWithComment(responseDtoResult);
                assertNull(responseDtoResult.getLastBooking());
                assertNull(responseDtoResult.getNextBooking());
                verify(bookingRepository, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(bookingRepository, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(commentRepository).findByItem_Id(anyLong());
            }
        }

        @Nested
        class WhenThrows {
            @Test
            @DisplayName("Получение не существующей вещи по id её собственником выбросит исключение")
            void getByOwnerById_thenBadItemId_thenNotFoundException() {
                //given
                when(itemRepository.findById(any())).thenReturn(Optional.empty());
                //when
                ItemNotFoundException e = assertThrows(ItemNotFoundException.class,
                        () -> itemService.getItemById(user1Id, item1Id)
                );
                //then
                assertEquals("Вещь с id = " + item1Id + " не найдена.", e.getMessage());
                verify(itemRepository, only()).findById(anyLong());
                verify(bookingRepository, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(bookingRepository, never()).findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                        anyLong(),
                        any(),
                        any());
                verify(commentRepository, never()).findByItem_Id(anyLong());
            }
        }
    }


    @Nested
    @DisplayName("Получение списка вещей")
    class GetAllTest {
        @Test
        @DisplayName("Получение списка всех вещей её владельцем с информацией о последнем завершённом и следующим будующим" +
                " + комментариями")
        void getAllByUserId_thenInputOk_thenOkAllDataAttached() {
            //given
            when(itemRepository.findItemByOwner_IdIs(anyLong(), any())).thenReturn(List.of(item1));
            when(userRepository.findById(user1Id)).thenReturn(Optional.of(user1));
            when(bookingRepository.findFirst1ByItemIdAndStartLessThanEqualAndStatusOrderByStartDesc(
                    anyLong(),
                    any(),
                    any())
            ).thenReturn(booking1Last);
            when(bookingRepository.findFirst1ByItemIdAndStartGreaterThanEqualAndStatusOrderByStartAsc(
                    anyLong(),
                    any(),
                    any())
            ).thenReturn(booking1Next);
            when(commentRepository.findByItem_Id(anyLong())).thenReturn(List.of(comment1));
            //when
            List<ItemBookingCommentDto> result = itemService.getOwnerItems(user1Id, 0, 20);
            //then
            assertThat(result).isNotNull();
            assertEquals(1, result.size());
            assertEquals(item1Id, result.get(0).getId());
            assertEquals(ITEM_NAME, result.get(0).getName());
            assertNotNull(result.get(0).getComments());
            assertEquals(1, result.get(0).getComments().size());
            assertEquals(COMMENT, result.get(0).getComments().get(0).getText());
            assertEquals(booking1Last.getId(), result.get(0).getLastBooking().getId());
            assertEquals(booking1Next.getId(), result.get(0).getNextBooking().getId());
        }
    }

    @Nested
    class SearchByQueryTest {
        @Nested
        class WhenQueryIsBlank {
            @ParameterizedTest
            @ValueSource(strings = {"", " "})
            @DisplayName("Поиск подходящих вещей по название/описанию с пустым или из пробелов query вернёт пустой список")
            void search_whenQueryIsBlank_thenEmptyList(String str) {
                //then
                assertTrue(itemService.findAvailableItemsByText(str, 0, 20).isEmpty());
            }
        }

        @Nested
        class WhenQueryIsCorrect {
            @Test
            @DisplayName("Поиск подходящих вещей по название/описанию с корректным query")
            void search_whenQuery_thenResult() {
                //given
                when(itemRepository.findItemByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(
                        anyString(), anyString(), any())).thenReturn(List.of(item1));
                //when
                List<ItemDto> result = itemService.findAvailableItemsByText("query", 0, 20);
                //then
                assertEquals(1, result.size());
                assertEquals(item1.getId(), result.get(0).getId());
                assertEquals(item1.getName(), result.get(0).getName());
            }
        }
    }

    @Nested
    class AddCommentTest {
        @Nested
        class WhenOk {
            @Test
            @DisplayName("Успешное добавление комментария")
            void addComment_whenInputOk_thenOk() {
                //given
                Booking booking = Booking.builder()
                        .id(1L)
                        .start(currentTime.minusMinutes(2))
                        .end(currentTime.minusMinutes(1))
                        .item(item1)
                        .booker(user2)
                        .status(Status.APPROVED)
                        .build();
                when(bookingRepository.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(
                        anyLong(), anyLong(), any(), any()))
                        .thenReturn(Optional.of(booking));
                when(commentRepository.save(any())).thenAnswer(
                        invocationOnMock -> {
                            Comment comment = invocationOnMock.getArgument(0, Comment.class);
                            comment.setId(1L);
                            return comment;
                        }
                );
                //when
                CommentResponseDto responseDtoResult = itemService.addComment(comment1Dto, item1Id, user2Id);
                //then
                assertThat(responseDtoResult).isNotNull();
                assertEquals(COMMENT, responseDtoResult.getText());
                assertEquals(user2.getName(), responseDtoResult.getAuthorName());
            }
        }

        @Nested
        class WhenThrows {
            @Test
            @DisplayName("Добавление комментария к не существующей вещи невозможно, выбросит исключение")
            void addComment_whenBookingNotFound_thenNotFoundException() {
                //given
                when(bookingRepository.findFirst1ByBookerIdAndItem_IdAndEndIsBeforeAndStatus(
                        anyLong(), anyLong(), any(), any()))
                        .thenReturn(Optional.empty());
                //when
                BadRequestException e = assertThrows(BadRequestException.class,
                        () -> itemService.addComment(comment1Dto, item1Id, user2Id)
                );
                //then
                assertEquals("Пользователь с id = " + user2Id + " не арендовал вещь.", e.getMessage());
                verify(commentRepository, never()).save(any());
            }
        }
    }

    private void setupUsersAndItemsAndDto() {
        currentTime = LocalDateTime.now();
        user1 = User.builder().id(user1Id).name("user1").email("user1@mail.com").build();
        user2 = User.builder().id(user2Id).name("user2").email("user2@mail.com").build();
        item1 = Item.builder().id(item1Id).owner(user1).name("item1").description("description1").available(true).build();
        item1Dto = ItemDto.builder()
                .name(ITEM_NAME)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .build();
        request1byUser2 = ItemRequest.builder()
                .id(1L)
                .description("request1ByUser2")
                .requester(user2)
                .created(currentTime)
                .build();
        booking1Last = Booking.builder()
                .id(1L)
                .start(currentTime.minusMinutes(2))
                .end(currentTime.minusMinutes(1))
                .item(item1)
                .booker(user2)
                .build();
        booking1Next = Booking.builder()
                .id(2L)
                .start(currentTime.plusMinutes(1))
                .end(currentTime.plusMinutes(2))
                .item(item1)
                .booker(user2)
                .build();
        comment1 = Comment.builder()
                .id(1L)
                .text(COMMENT)
                .item(item1)
                .author(user2)
                .build();
        comment1Dto = CommentDto.builder()
                .text(COMMENT)
                .build();
    }

    private void checkItemDtoBaseParam(ItemDto itemDto) {
        assertThat(itemDto).isNotNull();
        assertEquals(item1Id, itemDto.getId());
        assertEquals(ITEM_NAME, itemDto.getName());
        assertEquals(ITEM_DESCRIPTION, itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }

    private void checkItemBookingCommentDtoBaseParam(ItemBookingCommentDto itemDto) {
        assertThat(itemDto).isNotNull();
        assertEquals(item1Id, itemDto.getId());
        assertEquals(ITEM_NAME, itemDto.getName());
        assertEquals(ITEM_DESCRIPTION, itemDto.getDescription());
        assertTrue(itemDto.getAvailable());
    }

    private void checkItemDtoWithComment(ItemBookingCommentDto itemDto) {
        checkItemBookingCommentDtoBaseParam(itemDto);
        assertNotNull(itemDto.getComments());
        assertEquals(1, itemDto.getComments().size());
        assertEquals(comment1.getId(), itemDto.getComments().get(0).getId());
        assertEquals(comment1.getAuthor().getName(), itemDto.getComments().get(0).getAuthorName());
    }
}