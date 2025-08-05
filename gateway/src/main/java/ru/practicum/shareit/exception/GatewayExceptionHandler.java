package ru.practicum.shareit.exception;

import errors.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Component
@Slf4j
@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<ErrorResponse> handleGatewayException(final GatewayException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.getResponse());
    }

}
