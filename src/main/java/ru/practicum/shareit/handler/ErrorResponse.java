package ru.practicum.shareit.handler;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String name;
    private final String description;
}
