package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.User;
import user.UserCreateDto;
import user.UserDto;
import user.UserShortDto;

import java.util.Collection;

public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static UserShortDto toShortDto(User user) {
        return new UserShortDto(user.getId(), user.getName());
    }

    public static Collection<UserDto> toDto(Collection<User> users) {
        return users.stream().map(UserMapper::toDto).toList();
    }

    public static User toEntity(UserCreateDto createDto) {
        return new User(0, createDto.getName(), createDto.getEmail());
    }
}
