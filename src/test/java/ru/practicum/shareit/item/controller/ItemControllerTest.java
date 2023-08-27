package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemBookingCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mvc;
    private static final String PATH = "/items";
    private ItemDto itemDto;
    private ItemBookingCommentDto responseDto;
    private final long itemId = 1L;
    private final long userId = 1L;
    private final User user = User.builder().id(userId).email("user@mail.com").name("user").build();
    private final String itemName = "item";
    private final String itemDescription = "description";
    private LocalDateTime commentCreated;
    private final String commentText = "comment";

    @BeforeEach
    void setUp() {
        commentCreated = LocalDateTime.now();
        itemDto = ItemDto.builder()
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
        responseDto = ItemBookingCommentDto.builder()
                .id(1L)
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
    }

    @Test
    void contextLoad() {
        assertThat(itemService).isNotNull();
    }

    @Test
    void httpRequest_whenXSharerUserHeaderNotProvided_then_InternalServerError() throws Exception {
        //then
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH)).andExpect(status().isInternalServerError());
    }

    @Test
    void httpRequest_whenWrongXSharerUserHeader_thenBadRequest() throws Exception {
        //setup
        when(itemService.saveItem(any(ItemDto.class), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemService.updateItem(any(ItemDto.class), anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemService.getOwnerItems(anyLong(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("bad request message"));
        when(itemService.addComment(any(CommentDto.class), anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/search")).andExpect(status().isInternalServerError());
    }

    @Test
    void postItem_whenValidDtoAndUser_thenStatusOk() throws Exception {
        //given
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .build();
        when(itemService.saveItem(itemDto, userId)).thenReturn(response);
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.available", is(true), Boolean.class));
    }

    @Test
    void postItem_whenDtoWithRequestNotNullAndUser_thenStatusOk() throws Exception {
        //given
        long requestId = 1L;
        itemDto.setRequestId(requestId);
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name(itemName)
                .description(itemDescription)
                .available(true)
                .requestId(requestId)
                .build();
        when(itemService.saveItem(itemDto, userId, requestId)).thenReturn(response);
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(response.getName()), String.class))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.available", is(true), Boolean.class))
                .andExpect(jsonPath("$.requestId", is(response.getRequestId()), Long.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void postItem_whenDtoHasNoDescription_thenStatusBadRequest(String descr) throws Exception {
        //given
        ItemDto dtoEmptyDescription = ItemDto.builder().name(itemName).description(descr).available(true).build();
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoEmptyDescription))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void postItem_whenDtoHasNoName_thenStatusBadRequest(String name) throws Exception {
        //given
        ItemDto dtoEmptyName = ItemDto.builder().name(name).description(itemDescription).available(true).build();
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoEmptyName))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postItem_whenDtoHasNullValue_thenStatusBadRequest() throws Exception {
        //given
        ItemDto dtoNullAvailable = ItemDto.builder().name(itemName).description(itemDescription).build();
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(dtoNullAvailable))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        when(itemService.saveItem(any(ItemDto.class), anyLong())).thenThrow(new UserNotFoundException("not found message"));
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void patchItem_whenOnlyNameProvided_thenStatusOk() throws Exception {
        //given
        ItemDto onlyNameDto = ItemDto.builder().name("updated").build();
        when(itemService.updateItem(onlyNameDto, 1L, 1L)).thenReturn(itemDto);
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsString(onlyNameDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void patchItem_whenOnlyDescriptionProvided_thenStatusOk() throws Exception {
        //given
        ItemDto onlyNameDto = ItemDto.builder().description("updated").build();
        when(itemService.updateItem(onlyNameDto, 1L, 1L)).thenReturn(itemDto);
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsString(onlyNameDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void patchItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        when(itemService.updateItem(any(ItemDto.class), anyLong(), anyLong()))
                .thenThrow(new UserNotFoundException("not found message"));
        //when
        mvc.perform(patch(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, 2L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getItem_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        when(itemService.getItemById(anyLong(), anyLong())).thenThrow(new UserNotFoundException("not found message"));
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
    void getItem_whenValidData_thenStatusOk() throws Exception {
        //given
        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(responseDto);
        //when
        mvc.perform(get(PATH + "/{itemId}", "1")
                        .header(REQUEST_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk());
    }

    @Test
    void getAllByUserId_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        when(itemService.getOwnerItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(responseDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(responseDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(true), Boolean.class));
        verify(itemService).getOwnerItems(userId, 0, 20);
    }

    @Test
    void getAllByUserId_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        when(itemService.getOwnerItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, userId)
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk());
        verify(itemService).getOwnerItems(userId, 1, 2);
    }

    @Test
    void getAllByUserId_whenBadRequestParamFrom_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, userId)
                        .param("from", "-1")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).getOwnerItems(any(), any(), any());
    }

    @Test
    void getAllByUserId_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, userId)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        Mockito.verify(itemService, never()).getOwnerItems(any(), any(), any());
    }

    @Test
    void searchItems_whenQueryNotProvided_thenInternalServerError() throws Exception {
        //given
        when(itemService.findAvailableItemsByText(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));
        //when
        mvc.perform(get(PATH + "/search")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).findAvailableItemsByText(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItems_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        when(itemService.findAvailableItemsByText(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));
        //when
        mvc.perform(get(PATH + "/search")
                        .header(REQUEST_HEADER, userId)
                        .param("text", "query")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(true), Boolean.class));
        verify(itemService).findAvailableItemsByText("query", 1, 2);
    }

    @Test
    void searchItems_whenBadRequestParamFrom_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/search")
                        .header(REQUEST_HEADER, userId)
                        .param("from", "-1")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).findAvailableItemsByText(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItems_whenBadRequestParamSize_thenInternalServerError() throws Exception {
        //when
        mvc.perform(get(PATH + "/search")
                        .header(REQUEST_HEADER, userId)
                        .param("size", "0")
                        .characterEncoding(StandardCharsets.UTF_8))

                //then
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).findAvailableItemsByText(anyString(), anyInt(), anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", ""})
    void addComment_whenDtoBlankText_thenStatusBadRequest(String text) throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(text).build();
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
        verify(itemService, never()).addComment(any(), anyLong(), anyLong());
    }

    @Test
    void addComment_whenDtoHasNullText_thenStatusBadRequest() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().build();
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
        verify(itemService, never()).addComment(any(), anyLong(), anyLong());
    }

    @Test
    void addComment_whenNotFound_thenStatusNotFound() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        when(itemService.addComment(any(), anyLong(), anyLong()))
                .thenThrow(new BadRequestException("Пользователь с id = " + userId + " не арендовал вещь."));
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void addComment_whenValidDtoAndUser_thenStatusOk() throws Exception {
        //given
        CommentDto commentDto = CommentDto.builder().text(commentText).build();
        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("comment")
                .authorName(user.getName())
                .created(commentCreated)
                .build();
        when(itemService.addComment(commentDto, 1L, 1L)).thenReturn(commentResponseDto);
        //when
        mvc.perform(post(PATH + "/{itemId}/comment", itemId)
                        .header(REQUEST_HEADER, userId)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentResponseDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentResponseDto.getText()), String.class))
                .andExpect(jsonPath("$.authorName", is(user.getName()), String.class));
        verify(itemService, only()).addComment(commentDto, userId, itemId);
    }
}

