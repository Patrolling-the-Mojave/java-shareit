package ru.practicum.shareit.booking;

import booking.BookingCreateDto;
import booking.BookingDto;
import booking.enums.BookingStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import item.ItemShortDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.service.BookingService;
import user.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingCreateDto newBooking;

    private BookingDto bookingDto;

    private static final int USER_ID = 1;

    @BeforeEach
    void setup() {
        newBooking = new BookingCreateDto(1, LocalDateTime.now(), LocalDateTime.now().plusDays(10));
        bookingDto = new BookingDto(1, newBooking.getStart(), newBooking.getEnd(), new UserShortDto(1, "booker"), new ItemShortDto(1, "item"), BookingStatus.WAITING);
    }

    @Test
    public void testCreateBooking() throws Exception {
        when(bookingService.createBooking(newBooking, 1))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(newBooking))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.name", is("booker")));
    }

    @Test
    public void approveBookingTest_shouldReturnApprovedBooking() throws Exception {
        BookingDto approvedBooking = new BookingDto(1, bookingDto.getStart(), bookingDto.getEnd(), bookingDto.getBooker(), bookingDto.getItem(), BookingStatus.APPROVED);
        when(bookingService.approveBooking(1, USER_ID, true))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("approved", String.valueOf(true))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getBookingById_shouldReturnBooking() throws Exception {
        when(bookingService.findById(1, USER_ID)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getAllBookingsOfUser_shouldReturnBookings() throws Exception {
        BookingDto bookingDto2 = new BookingDto(2,
                LocalDateTime.now().plusDays(20),
                LocalDateTime.now().plusDays(10),
                bookingDto.getBooker(),
                new ItemShortDto(2, "secondItem"),
                BookingStatus.APPROVED
        );

        when(bookingService.findAllByBookerAndState(USER_ID, "ALL"))
                .thenReturn(List.of(bookingDto, bookingDto2));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAllBookingsByOwner_shouldReturnBookings() throws Exception {
        when(bookingService.findAllByItemOwnerAndState(USER_ID, "ALL"))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }
}

