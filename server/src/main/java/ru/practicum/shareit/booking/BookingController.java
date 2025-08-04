package ru.practicum.shareit.booking;

import booking.BookingCreateDto;
import booking.BookingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestBody BookingCreateDto newBooking, @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return bookingService.createBooking(newBooking, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable int bookingId,
                                     @RequestHeader(name = "X-Sharer-User-Id") int userId,
                                     @RequestParam(name = "approved") boolean approved) {
        return bookingService.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findById(@PathVariable int bookingId,
                               @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return bookingService.findById(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingDto> findAllBookingsOfUser(@RequestHeader(name = "X-Sharer-User-Id") int userId,
                                                        @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllByBookerAndState(userId, state);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> findAllBookingsByItemsOwner(@RequestHeader(name = "X-Sharer-User-Id") int ownerId,
                                                              @RequestParam(name = "state", required = false, defaultValue = "ALL") String state) {
        return bookingService.findAllByItemOwnerAndState(ownerId, state);
    }
}
