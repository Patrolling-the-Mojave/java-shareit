package ru.practicum.shareit.handler;

import errors.ErrorResponse;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;

@Component
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(final NotFoundException exception) {
        log.error("not found", exception);
        return new ErrorResponse("not found", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleOwnerHasNoItem(final OwnerHasNoItemsException exception) {
        log.error("not found", exception);
        return new ErrorResponse("not found", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleNoAccess(final NoAccessException exception) {
        log.error("no access", exception);
        return new ErrorResponse("no access", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException exception) {
        log.error("validation exception", exception);
        return new ErrorResponse("not valid", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(final IllegalArgumentException exception) {
        log.error("illegal argument", exception);
        return new ErrorResponse("illegal argument", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailExistsException(final EmailAlreadyExistException exception) {
        log.error("email already exists", exception);
        return new ErrorResponse("email already exist", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInaccessibleItem(final InaccessibleItemException exception) {
        log.error("inaccessible item", exception);
        return new ErrorResponse("inaccessible item", exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final RuntimeException exception) {
        log.error("error", exception);
        return new ErrorResponse("error", exception.getMessage());
    }
}
