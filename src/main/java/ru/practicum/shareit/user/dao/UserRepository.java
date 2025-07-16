package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User create(User user);

    User update(User newUser);

    void delete(int id);

    Optional<User> findById(int id);

    Collection<User> findAll();
}
