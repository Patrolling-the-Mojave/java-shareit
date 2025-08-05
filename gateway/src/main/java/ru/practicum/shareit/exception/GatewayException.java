package ru.practicum.shareit.exception;

import errors.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GatewayException extends RuntimeException {
    private HttpStatus status;
    private ErrorResponse response;

    public GatewayException(HttpStatus status, ErrorResponse response) {
        super(response.getDescription());
        this.status = status;
        this.response = response;
    }
}
