package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.exception.InaccessibleItemException;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.OwnerHasNoItemsException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;

import static java.lang.String.format;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toDto;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toEntity;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto createBooking(BookingCreateDto newBooking, int userId) {
        Item item = getItemById(newBooking.getItemId());
        User user = getUserById(userId);
        if (!item.getAvailable()) {
            throw new InaccessibleItemException("предмет с id " + item.getId() + " уже занят");
        }
        if (newBooking.getStart().isAfter(newBooking.getEnd())) {
            throw new IllegalArgumentException("начало бронирования не должно быть позже конца");
        }
        Booking booking = toEntity(newBooking, user, item);
        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto approveBooking(int bookingId, int userId, boolean approved) {
        Booking booking = getBookingById(bookingId);
        if (booking.getItem().getOwner().getId() != userId) {
            throw new NoAccessException(format("пользователь %d не является владельцем вещи %d", userId, bookingId));
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto findById(int bookingId, int userId) {
        Booking booking = getBookingById(bookingId);
        User user = getUserById(userId);
        if (!(booking.getBooker().equals(user) || booking.getItem().getOwner().equals(user))) {
            throw new NoAccessException("у пользователя " + userId + " нет доступа к бронированию " + bookingId);
        }
        return toDto(booking);
    }

    @Override
    public Collection<BookingDto> findAllByBookerAndState(int bookerId, String state) {
        getUserById(bookerId);
        final BookingState bookingState = getBookingStateFromString(state);
        Collection<Booking> bookings;
        switch (bookingState) {
            case ALL -> bookings = bookingRepository.findAllByBookerId(bookerId);
            case CURRENT -> bookings = bookingRepository.findAllCurrentByBooker(bookerId);
            case PAST -> bookings = bookingRepository.findAllPastByBooker(bookerId);
            case FUTURE -> bookings = bookingRepository.findAllFutureByBooker(bookerId);
            case WAITING -> bookings = bookingRepository.findAllWaitingByBooker(bookerId);
            case REJECTED -> bookings = bookingRepository.findAllRejectedByBooker(bookerId);
            default -> throw new IllegalArgumentException("неизвестное состояние: " + bookingState);
        }
        return toDto(bookings);
    }

    @Override
    public Collection<BookingDto> findAllByItemOwnerAndState(int ownerId, String state) {
        getUserById(ownerId);
        final BookingState bookingState = getBookingStateFromString(state);
        if (!itemRepository.existByOwnerId(ownerId)) {
            throw new OwnerHasNoItemsException("У пользователя " + ownerId + " нет предметов для бронирования");
        }
        Collection<Booking> bookings;
        switch (bookingState) {
            case ALL -> bookings = bookingRepository.findAllByItemOwner(ownerId);
            case CURRENT -> bookings = bookingRepository.findAllCurrentByItemOwner(ownerId);
            case PAST -> bookings = bookingRepository.findAllPastByItemOwner(ownerId);
            case FUTURE -> bookings = bookingRepository.findAllFutureByItemOwner(ownerId);
            case WAITING -> bookings = bookingRepository.findAllWaitingByItemOwner(ownerId);
            case REJECTED -> bookings = bookingRepository.findAllRejectedByItemOwner(ownerId);
            default -> throw new IllegalArgumentException("неизвестное состояние: " + bookingState);
        }
        return toDto(bookings);
    }


    private Booking getBookingById(int bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("бронирование с id " + bookingId + " не найдено"));
    }

    private User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("пользователь с id " + id + " не найден"));
    }

    private Item getItemById(int id) {
        return itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException("предмет с id " + id + " не найден"));
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
