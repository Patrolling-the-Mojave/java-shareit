package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public class UserMapper {
    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static Collection<UserDto> toDto(Collection<User> users) {
        return users.stream().map(UserMapper::toDto).toList();
    }

    public static User toEntity(UserCreateDto createDto) {
        return new User(0, createDto.getName(), createDto.getEmail());
    }
}
