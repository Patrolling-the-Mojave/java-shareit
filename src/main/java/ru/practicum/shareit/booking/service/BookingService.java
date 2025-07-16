package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(BookingCreateDto newBooking, int userId);

    BookingDto approveBooking(int bookingId, int userId, boolean approved);

    BookingDto findById(int bookingId, int userId);

    Collection<BookingDto> findAllByBookerAndState(int userId, String state);

    Collection<BookingDto> findAllByItemOwnerAndState(int ownerId, String state);
}
