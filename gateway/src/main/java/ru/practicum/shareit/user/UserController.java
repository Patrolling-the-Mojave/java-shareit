package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import user.UserCreateDto;
import user.UserDto;
import user.UserUpdateDto;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public UserDto create(@Validated @RequestBody UserCreateDto newUser) {
        return userClient.createUser(newUser);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@Validated @RequestBody UserUpdateDto updatedUser, @PathVariable int userId) {
        return userClient.updateUser(updatedUser, userId);
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        return userClient.findAll();
    }

    @GetMapping("/{userId}")
    public UserDto findById(@PathVariable int userId) {
        return userClient.findById(userId);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        userClient.delete(userId);
    }
}
