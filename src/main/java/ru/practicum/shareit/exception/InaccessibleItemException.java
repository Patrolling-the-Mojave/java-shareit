package ru.practicum.shareit.exception;

public class InaccessibleItemException extends RuntimeException {
    public InaccessibleItemException(String message) {
        super(message);
    }
}
