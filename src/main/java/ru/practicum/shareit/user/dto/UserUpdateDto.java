package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    private Integer id;
    @Size(max = 25)
    private String name;
    @Email
    private String email;

}
