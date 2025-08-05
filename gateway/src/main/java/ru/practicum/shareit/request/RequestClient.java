package ru.practicum.shareit.request;

import errors.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.exception.GatewayException;

import java.util.Collection;

import static ru.practicum.shareit.utils.ErrorParser.toErrorResponse;

@Component
@RequiredArgsConstructor
public class RequestClient {
    private static final String REQUEST_URI = "/requests";
    private final WebClient webClient;

    public ItemRequestDto createRequest(RequestCreateDto newRequest, int requesterId) {
        try {
            return webClient
                    .post()
                    .uri(REQUEST_URI)
                    .header("X-Sharer-User-Id", String.valueOf(requesterId))
                    .bodyValue(newRequest)
                    .retrieve()
                    .bodyToMono(ItemRequestDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<ItemRequestDto> findAll() {
        try {
            return webClient
                    .get()
                    .uri(REQUEST_URI + "/all")
                    .retrieve()
                    .bodyToFlux(ItemRequestDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<RequestWithAnswersDto> findByRequesterId(int requesterId) {
        try {
            return webClient
                    .get()
                    .uri(REQUEST_URI)
                    .header("X-Sharer-User-Id", String.valueOf(requesterId))
                    .retrieve()
                    .bodyToFlux(RequestWithAnswersDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public RequestWithAnswersDto findById(int requestId) {
        try {
            return webClient
                    .get()
                    .uri(REQUEST_URI + "/" + requestId)
                    .retrieve()
                    .bodyToMono(RequestWithAnswersDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }
}
