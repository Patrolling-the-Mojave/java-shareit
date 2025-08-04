package ru.practicum.shareit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import errors.ErrorResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class ErrorParser {
    public static ErrorResponse toErrorResponse(WebClientResponseException exception) {
        try {
            return new ObjectMapper().readValue(exception.getResponseBodyAsString(), ErrorResponse.class);
        } catch (JsonProcessingException ex) {
            return new ErrorResponse("error", exception.getResponseBodyAsString());
        }
    }
}
