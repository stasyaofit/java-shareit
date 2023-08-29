package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;
import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String PATH = "/requests";
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    private final Long userId = 1L;
    private ItemRequestShortDto requestDto;
    private ItemRequestResponseDto itemRequestResponseDto;
    LocalDateTime created = LocalDateTime.now();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @BeforeEach
    void setup() {
        requestDto = ItemRequestShortDto.builder()
                .description("itemRequest")
                .build();
        itemRequestResponseDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("itemRequest")
                .created(created)
                .build();
    }

    @Test
    void contextLoad() {
        assertThat(itemRequestService).isNotNull();
    }

    @Test
    void httpRequest_whenXSharerUserHeaderNotProvided_then_InternalServerError() throws Exception {
        //then
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/all")).andExpect(status().isInternalServerError());
    }

    @Test
    void httpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //setup
        when(itemRequestService.addItemRequest(anyLong(), any(ItemRequestShortDto.class)))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemRequestService.getOwnRequests(anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemRequestService.getRequestById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/all")).andExpect(status().isInternalServerError());
    }

    @Test
    void addNewItemRequest_whenInputDataOk_thenStatusOk() throws Exception {
        //given
        when(itemRequestService.addItemRequest(userId, requestDto)).thenReturn(itemRequestResponseDto);
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestResponseDto.getCreated().format(dtf))));
        verify(itemRequestService, times(1)).addItemRequest(eq(userId), any(ItemRequestShortDto.class));
    }

    @Test
    void addNewItemRequest_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        when(itemRequestService.addItemRequest(anyLong(), any()))
                .thenThrow(new UserNotFoundException("not found message"));
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequestsByAnotherUsers_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestResponseDto));

        //when
        mvc.perform(get(PATH + "/all")
                        .header(REQUEST_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().format(dtf))));
        verify(itemRequestService).getAllRequests(2L, 0, 20);
    }

    @Test
    void getAllRequestsByAnotherUsers_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        when(itemRequestService.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(itemRequestResponseDto));
        //when
        mvc.perform(get(PATH + "/all")
                        .header(REQUEST_HEADER, 2L)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        verify(itemRequestService).getAllRequests(2L, 1, 2);
    }

    @Test
    void getRequestById_whenValidData_thenStatusOk() throws Exception {
        //given
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestResponseDto);
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenThrow(new NotFoundException("not found message"));
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, 2L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getOwnRequests_whenValidData_thenStatusOk() throws Exception {
        //given
        long ownerId = 2L;
        List<ItemRequestResponseDto> responseDtoList = List.of(itemRequestResponseDto);
        when(itemRequestService.getOwnRequests(anyLong())).thenReturn(responseDtoList);
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemRequestResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestResponseDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(itemRequestResponseDto.getCreated().format(dtf))));
        verify(itemRequestService).getOwnRequests(ownerId);
    }

    @Test
    void getOwnRequests_whenOwnerNotFound_thenStatusNotFound() throws Exception {
        long ownerId = 3L;
        when(itemRequestService.getOwnRequests(anyLong()))
                .thenThrow(new UserNotFoundException("Пользователь не найден"));
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, ownerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }
}