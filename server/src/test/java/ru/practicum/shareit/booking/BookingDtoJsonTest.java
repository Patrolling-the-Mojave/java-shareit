package ru.practicum.shareit.booking;

import booking.BookingDto;
import booking.enums.BookingStatus;
import item.ItemShortDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import user.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1)
                .start(LocalDateTime.of(2023, 1, 1, 12, 0))
                .end(LocalDateTime.of(2023, 1, 2, 12, 0))
                .status(BookingStatus.APPROVED)
                .booker(new UserShortDto(1, "User"))
                .item(new ItemShortDto(1, "Item"))
                .build();

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2023-01-01T12:00:00");
    }
}

