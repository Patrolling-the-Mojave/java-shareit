package ru.practicum.shareit.booking.mapper;

import booking.BookingCreateDto;
import booking.BookingDto;
import booking.enums.BookingStatus;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;

public class BookingMapper {

    public static Booking toEntity(BookingCreateDto createDto, User user, Item item) {
        return Booking.builder()
                .start(createDto.getStart())
                .end(createDto.getEnd())
                .booker(user)
                .item(item)
                .status(BookingStatus.WAITING)
                .build();
    }

    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserMapper.toShortDto(booking.getBooker()))
                .item(ItemMapper.toShortDto(booking.getItem()))
                .build();
    }

    public static Collection<BookingDto> toDto(Collection<Booking> bookings) {
        return bookings.stream().map(BookingMapper::toDto).toList();
    }
}
