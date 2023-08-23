package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemForRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.practicum.shareit.util.Constants.DATE_TIME_FORMAT;

@JsonTest
class ItemRequestResponseDtoTest {
    @Autowired
    private JacksonTester<ItemRequestResponseDto> jacksonTester;

    @Test
    void serializeJsonTest() throws IOException {
        //given
        LocalDateTime created = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("itemRequestDescription")
                .created(created)
                .items(List.of(ItemForRequestDto.builder()
                        .id(1L)
                        .name("item1")
                        .ownerId(1L)
                        .description("itemDescription")
                        .requestId(1L)
                        .available(true)
                        .build()))
                .build();
        //when
        JsonContent<ItemRequestResponseDto> jsonContent = jacksonTester.write(dto);
        //then
        assertThat(jsonContent).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.description").isEqualTo("itemRequestDescription");
        assertThat(jsonContent).extractingJsonPathStringValue("$.created").isEqualTo(created.format(dtf));
        assertThat(jsonContent).extractingJsonPathNumberValue("$.items.length()").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].name").isEqualTo("item1");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathStringValue("$.items[0].description").isEqualTo("itemDescription");
        assertThat(jsonContent).extractingJsonPathNumberValue("$.items[0].requestId").isEqualTo(1);
        assertThat(jsonContent).extractingJsonPathBooleanValue("$.items[0].available").isTrue();
    }
}