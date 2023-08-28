package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ShortItemDto;
import ru.practicum.shareit.user.dto.ShortUserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;
import static ru.practicum.shareit.util.Constants.REQUEST_HEADER;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;
    private static final String PATH = "/bookings";
    private BookingRequestDto bookingDto;
    private BookingResponseDto responseDto;
    private final long bookerId = 1L;
    private final long bookingId = 1L;
    private LocalDateTime startBooking;
    private LocalDateTime endBooking;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @BeforeEach
    void setup() {
        startBooking = LocalDateTime.now().plusMinutes(1);
        endBooking = startBooking.plusMinutes(1);
    }

    @Test
    void contextLoad() {
        assertThat(bookingService).isNotNull();
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
        //given
        when(bookingService.addBooking(any(BookingRequestDto.class), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(new BadRequestException("bad request message"));
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("bad request message"));
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("bad request message"));
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenThrow(new BadRequestException("bad request message"));
        //when - then expect
        mvc.perform(post(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH)).andExpect(status().isInternalServerError());
        mvc.perform(get(PATH + "/owner")).andExpect(status().isInternalServerError());
        mvc.perform(patch(PATH)).andExpect(status().isInternalServerError());
    }

    @Test
    void postBooking_whenValidDtoAndBooker_thenStatusOk() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.addBooking(bookingDto, bookerId)).thenReturn(responseDto);
        //when
        mvc.perform(post(PATH)
                        .header(REQUEST_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void postBooking_whenDtoHasEndDateInPast_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking.minusMinutes(10));
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(REQUEST_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasStartDateEqualsEndDates_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, startBooking);
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(REQUEST_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        //
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void postBooking_whenDtoHasEndDateEarlierStartDates_thenStatusBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, startBooking.minusSeconds(30));
        //when
        mvc.perform(post(PATH)
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .header(REQUEST_HEADER, 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_whenOwnerIdIsOkNewStatusApproved_thenStatusOk() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(responseDto);
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header(REQUEST_HEADER, ownerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void approveBooking_whenPathVariableBookingIdNotProvided_thenStatus500() throws Exception {
        //when
        mvc.perform(patch(PATH)
                        .param("approved", "true")
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    void approveBooking_whenRequestParamApprovedNotProvided_thenStatus500() throws Exception {
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //when
                .andExpect(status().isInternalServerError());
    }

    @ParameterizedTest
    @ValueSource(strings = {"any", ""})
    void approveBooking_whenRequestParamApprovedIsWrong_thenStatus500(String str) throws Exception {
        //when
        mvc.perform(patch(PATH + "/{bookingId}", bookingId)
                        .param("approved", str)
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getBookingById_whenBookingIsPresent_thenStatusOk() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(responseDto);
        //when
        mvc.perform(get(PATH + "/{bookingId}", bookingId)
                        .param("approved", "true")
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(responseDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$.end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$.item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(responseDto.getStatus().toString())));
    }

    @Test
    void getBookingById_whenServiceThrowsNotFound_thenStatusNotFound() throws Exception {
        //given
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new UserNotFoundException("not found message"));
        //when
        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookingByBooker_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, bookerId)
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$[0].end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$[0].item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(responseDto.getStatus().toString())));
        verify(bookingService).getUserBookings(bookerId, BookingState.ALL, 0, 20);
    }

    @Test
    void getBookingByBooker_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, bookerId)
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        verify(bookingService).getUserBookings(bookerId, BookingState.CURRENT, 1, 2);
    }

    @Test
    void getBookingByBooker_whenBadParamsForStatus_thenBadRequest() throws Exception {
        //given
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getUserBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH)
                        .header(REQUEST_HEADER, bookerId)
                        .param("state", "any")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isBadRequest());
        verify(bookingService, never()).getUserBookings(anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void getBookingByOwner_whenRequestParamsNotProvided_thenOKAndDefaultValues() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(REQUEST_HEADER, ownerId) //
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].start", is(responseDto.getStart().format(dtf))))
                .andExpect(jsonPath("$[0].end", is(responseDto.getEnd().format(dtf))))
                .andExpect(jsonPath("$[0].item.id", is(responseDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(responseDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is(responseDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(responseDto.getStatus().toString())));
        verify(bookingService).getOwnerBookings(ownerId, BookingState.ALL, 0, 20);
    }

    @Test
    void getBookingByOwner_whenRequestParamsProvided_thenOKAndParamsValues() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(REQUEST_HEADER, ownerId) //any id for owner
                        .param("state", "current")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isOk());
        verify(bookingService).getOwnerBookings(ownerId, BookingState.CURRENT, 1, 2);
    }

    @Test
    void getBookingByOwner_whenBadParamsForStatus_thenBadRequest() throws Exception {
        //given
        long ownerId = 2L;
        setupEntityDtos(startBooking, endBooking);
        when(bookingService.getOwnerBookings(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(responseDto));
        //when
        mvc.perform(get(PATH + "/owner")
                        .header(REQUEST_HEADER, ownerId)
                        .param("state", "any")
                        .param("from", "1")
                        .param("size", "2")
                        .characterEncoding(StandardCharsets.UTF_8))
                //then
                .andExpect(status().isBadRequest());
        verify(bookingService, never()).getOwnerBookings(anyLong(), any(), anyInt(), anyInt());
    }

    private void setupEntityDtos(LocalDateTime start, LocalDateTime end) {
        long itemId = 1L;
        bookingDto = BookingRequestDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .build();
        responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .status(Status.WAITING)
                .item(ShortItemDto.builder().id(itemId).name("item name").build())
                .booker(ShortUserDto.builder().id(bookerId).build())
                .build();
    }
}