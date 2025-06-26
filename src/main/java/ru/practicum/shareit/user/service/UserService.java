package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExists;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.Collection;

import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;
import static ru.practicum.shareit.util.Updater.runIfNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserDto create(final UserCreateDto newUser) {
        doesEmailExists(newUser.getEmail(), 0);
        User user = UserMapper.newUserDtoToUser(newUser);
        userRepository.create(user);
        return toUserDto(user);
    }

    public UserDto update(final UserUpdateDto updatedUser, final int id) {
        User oldUser = getUserById(id);
        doesEmailExists(updatedUser.getEmail(), id);
        runIfNotNull(updatedUser.getName(), () -> oldUser.setName(updatedUser.getName()));
        runIfNotNull(updatedUser.getEmail(), () -> oldUser.setEmail(updatedUser.getEmail()));
        userRepository.update(oldUser);
        return toUserDto(oldUser);
    }

    public void delete(int id) {
        userRepository.delete(id);
    }

    public Collection<UserDto> findAll() {
        return userRepository
                .findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto findById(int id) {
        return UserMapper.toUserDto(getUserById(id));
    }

    private User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("пользователь с id " + id + " не найден"));
    }

    private void doesEmailExists(final String email, final int userId) {
        if (userRepository.findAll().stream().anyMatch(user1 -> user1.getEmail().equals(email) && user1.getId() != userId)) {
            throw new EmailAlreadyExists(String.format("Email %s уже существует", email));
        }
    }
}
