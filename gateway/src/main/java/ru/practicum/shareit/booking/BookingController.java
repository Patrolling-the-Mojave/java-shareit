package ru.practicum.shareit.booking;

import booking.BookingCreateDto;
import booking.BookingDto;
import booking.enums.BookingState;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@Validated @RequestBody BookingCreateDto newBooking,
                                    @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return bookingClient.createBooking(newBooking, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable int bookingId,
                                     @RequestHeader(name = "X-Sharer-User-Id") int userId,
                                     @RequestParam(name = "approved") boolean approved) {
        return bookingClient.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable int bookingId,
                               @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return bookingClient.findById(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingDto> findAllBookingsOfUser(@RequestHeader(name = "X-Sharer-User-Id") int userId,
                                                        @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = getBookingStateFromString(state);
        return bookingClient.findAllByBookerAndState(userId, bookingState.name());
    }

    @GetMapping("/owner")
    public Collection<BookingDto> findAllBookingsByItemsOwner(@RequestHeader(name = "X-Sharer-User-Id") int ownerId,
                                                              @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        BookingState bookingState = getBookingStateFromString(state);
        return bookingClient.findAllByItemOwnerAndState(ownerId, bookingState.name());
    }

    private BookingState getBookingStateFromString(String state) {
        final BookingState bookingState;
        try {
            bookingState = BookingState.valueOf(state.toUpperCase().trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(String.format("состояние бронирования %s не поддерживается", state));
        }
        return bookingState;
    }
}
