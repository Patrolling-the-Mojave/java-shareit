package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    public Collection<BookingDto> findAllByBookerAndState(int userId, String state) {
        getUserById(userId);
        return toDto(bookingRepository.findByBookerAndState(userId, state));
    }

    @Override
    public Collection<BookingDto> findAllByItemOwnerAndState(int ownerId, String state) {
        getUserById(ownerId);
        if (!itemRepository.existByOwnerId(ownerId)) {
            throw new OwnerHasNoItemsException("У пользователя " + ownerId + " нет предметов для бронирования");
        }
        return toDto(bookingRepository.findByOwnerIdAndState(ownerId, state));
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
}
