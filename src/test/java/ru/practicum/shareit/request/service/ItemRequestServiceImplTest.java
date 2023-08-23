package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositoty.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {

    @InjectMocks
    private final ItemRequestService requestService;

    @MockBean
    private final ItemRequestRepository requestRepository;

    @MockBean
    private final UserRepository userRepository;

    @MockBean
    private final ItemRepository itemRepository;
    private User user1;
    private User user2;
    private static final Long VALUE_ID_1 = 1L;
    private final Long user1Id = VALUE_ID_1;
    private final Long user2Id = 2L;
    private Item item;
    private ItemRequestShortDto requestByUser2Dto;
    private final Long requestByUser2Id = VALUE_ID_1;
    private ItemRequest requestByUser2;
    private LocalDateTime currentTime;

    @BeforeEach
    void setup() {
        setupUsersAndItemsAndDto();
    }

    @Test
    @DisplayName("Успешное добавление запроса")
    void addRequest_whenInputOk_thenOk() {
        //given
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        when(requestRepository.save(any())).thenAnswer(
                invocationOnMock -> {
                    ItemRequest i = invocationOnMock.getArgument(0, ItemRequest.class);
                    i.setId(requestByUser2Id);
                    return i;
                }
        );
        //when
        ItemRequestResponseDto responseDtoResult = requestService.addItemRequest(user2Id, requestByUser2Dto);
        //then
        assertThat(responseDtoResult).isNotNull();
        assertEquals(requestByUser2Id, responseDtoResult.getId());
        assertTrue(responseDtoResult.getItems().isEmpty());
    }

    @Test
    @DisplayName("Добавление запроса несуществующим пользователем")
    void addRequest_whenUserNotFound_thenNotFoundException() {
        //given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        //when
        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () -> requestService.addItemRequest(user2Id, requestByUser2Dto)
        );
        //then
        assertEquals("Пользователь с id = " + user2Id + " не найден.", e.getMessage());
        verify(userRepository, only()).findById(anyLong());
        verify(requestRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Получение списка запросов собственника, если их нет, то возвращает пустой список")
    void getRequestsByUserId_thenInputOkWithItems_thenWithItems() {
        //given
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(user2Id)).thenReturn(List.of(requestByUser2));
        when(itemRepository.findAllByRequest_IdIn(any())).thenReturn(List.of(item));
        //when
        List<ItemRequestResponseDto> requests = requestService.getOwnRequests(user2Id);
        //then
        assertThat(requests).isNotNull();
        assertEquals(1, requests.size());
        assertEquals(requestByUser2Id, requests.get(0).getId());
        assertEquals(1, requests.get(0).getItems().get(0).getId());
        assertEquals("item", requests.get(0).getItems().get(0).getName());
        assertEquals("itemRequest", requests.get(0).getDescription());
        assertEquals(currentTime, requests.get(0).getCreated());
    }


    @Test
    @DisplayName("Получение списка запросов другого пользователя, если их нет, то возвращает пустой список")
    void getAllRequestsByAnotherUsers_thenInputOkWithItems_thenWithItems() {
        //given
        when(requestRepository.findAllByRequesterIdNot(
                anyLong(),
                any())).thenReturn(List.of(requestByUser2));
        when(itemRepository.findAllByRequest_IdIn(any())).thenReturn(List.of(item));
        //when
        List<ItemRequestResponseDto> requests = requestService.getAllRequests(user1Id, 0, 20);
        //then
        assertThat(requests).isNotNull();
        assertEquals(1, requests.size());
        assertEquals(requestByUser2Id, requests.get(0).getId());
        assertEquals(1, requests.get(0).getItems().get(0).getId());
        assertEquals("item", requests.get(0).getItems().get(0).getName());
        assertEquals("itemRequest", requests.get(0).getDescription());
        assertEquals(currentTime, requests.get(0).getCreated());
    }

    @Test
    @DisplayName("Успешное получение запроса по id")
    void getRequestById_whenInputOk_thenOk() {
        //given
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        when(requestRepository.findById(requestByUser2Id)).thenReturn(Optional.of(requestByUser2));
        when(itemRepository.findAllByRequest_IdIn(any())).thenReturn(List.of(item));
        //when
        ItemRequestResponseDto responseDto = requestService.getRequestById(user2Id, requestByUser2Id);
        //then
        assertThat(responseDto).isNotNull();
        assertEquals(requestByUser2Id, responseDto.getId());
        assertEquals(1, responseDto.getItems().size());
        assertEquals(1, responseDto.getItems().get(0).getId());
        assertEquals("item", responseDto.getItems().get(0).getName());
        assertEquals("itemRequest", responseDto.getDescription());
        assertEquals(currentTime, responseDto.getCreated());
    }

    @Test
    @DisplayName("Получение запроса по id не существующим пользователем выбросит исключение")
    void getRequestById_whenUserNotFound_thenThrowsUserNotFound() {

        //given
        when(userRepository.findById(user2Id)).thenReturn(Optional.empty());
        //when
        UserNotFoundException e = assertThrows(UserNotFoundException.class,
                () ->
                        requestService.getRequestById(user2Id, requestByUser2Id)
        );
        //then
        assertEquals("Пользователь с id = " + user2Id + " не найден.", e.getMessage());
        verify(userRepository, only()).findById(anyLong());
        verify(requestRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findAllByRequest_IdIn(any());
    }

    @Test
    @DisplayName("Получение не существующего запроса по id выбросит исключение")
    void getRequestById_whenRequestNotFound_thenThrowsNotFound() {
        //given
        when(userRepository.findById(user2Id)).thenReturn(Optional.of(user2));
        when(requestRepository.findById(requestByUser2Id)).thenReturn(Optional.empty());
        //when
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(user2Id, requestByUser2Id)
        );
        //then
        assertEquals("Запроc c id = " + requestByUser2Id + " не найден.", e.getMessage());
        verify(userRepository, only()).findById(anyLong());
        verify(requestRepository, only()).findById(anyLong());
        verify(itemRepository, never()).findAllByRequest_IdIn(any());
    }

    private void setupUsersAndItemsAndDto() {
        currentTime = LocalDateTime.now();
        user1 = User.builder().id(VALUE_ID_1).name("user1").email("user1@mail.com").build();
        user2 = User.builder().id(user2Id).name("user2").email("user2@mail.com").build();
        requestByUser2Dto = ItemRequestShortDto.builder()
                .description("itemRequest")
                .build();
        requestByUser2 = ItemRequest.builder()
                .id(requestByUser2Id)
                .description("itemRequest")
                .requester(user2)
                .created(currentTime)
                .build();
        item = Item.builder()
                .id(VALUE_ID_1)
                .name("item")
                .description("description")
                .available(true)
                .owner(user1)
                .request(requestByUser2)
                .build();
    }
}
