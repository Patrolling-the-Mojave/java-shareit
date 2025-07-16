package ru.practicum.shareit.exception;

public class OwnerHasNoItemsException extends RuntimeException {
    public OwnerHasNoItemsException(String message) {
        super(message);
    }
}
