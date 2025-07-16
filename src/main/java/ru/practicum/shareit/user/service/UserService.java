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

import java.util.Collection;

import static ru.practicum.shareit.user.mapper.UserMapper.toDto;
import static ru.practicum.shareit.user.mapper.UserMapper.toEntity;
import static ru.practicum.shareit.util.Updater.runIfNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserDto create(final UserCreateDto newUser) {
        log.trace("запрос на создание пользователя");
        doesEmailExists(newUser.getEmail(), 0);
        User user = toEntity(newUser);
        log.debug("пользователь добавлен{}", user);
        return toDto(userRepository.save(user));
    }

    public UserDto update(final UserUpdateDto updatedUser, final int id) {
        User oldUser = getUserById(id);
        doesEmailExists(updatedUser.getEmail(), id);
        runIfNotNull(updatedUser.getName(), () -> oldUser.setName(updatedUser.getName()));
        runIfNotNull(updatedUser.getEmail(), () -> oldUser.setEmail(updatedUser.getEmail()));
        userRepository.save(oldUser);
        log.debug("пользователь обновлен {}", oldUser);
        return toDto(oldUser);
    }

    public void delete(int id) {
        getUserById(id);
        log.debug("пользователь {} удален", id);
        userRepository.deleteById(id);
    }

    public Collection<UserDto> findAll() {
        log.debug("запрос всех пользователей");
        return toDto(userRepository.findAll());
    }

    public UserDto findById(int id) {
        return toDto(getUserById(id));
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
