package ru.practicum.shareit.user;

import errors.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.practicum.shareit.exception.GatewayException;
import user.UserCreateDto;
import user.UserDto;
import user.UserUpdateDto;

import java.util.Collection;

import static ru.practicum.shareit.utils.ErrorParser.toErrorResponse;

@RequiredArgsConstructor
@Component
public class UserClient {
    private static final String USER_URI = "/users";
    private final WebClient webClient;

    public UserDto createUser(UserCreateDto newUser) {
        try {
            return webClient
                    .post()
                    .uri(USER_URI)
                    .bodyValue(newUser)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public UserDto updateUser(UserUpdateDto updatedUser, int userId) {
        try {
            return webClient
                    .patch()
                    .uri(USER_URI + "/" + userId)
                    .bodyValue(updatedUser)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<UserDto> findAll() {
        try {
            return webClient
                    .get()
                    .uri(USER_URI)
                    .retrieve()
                    .bodyToFlux(UserDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public UserDto findById(int userId) {
        try {
            return webClient
                    .get()
                    .uri(USER_URI + "/" + userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public void delete(int userId) {
        try {
            webClient.delete().uri(USER_URI + "/" + userId);
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }

    }

}
