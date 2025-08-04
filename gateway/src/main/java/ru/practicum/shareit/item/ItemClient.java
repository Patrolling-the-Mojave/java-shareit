package ru.practicum.shareit.item;

import errors.ErrorResponse;
import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.practicum.shareit.exception.GatewayException;

import java.util.Collection;

import static ru.practicum.shareit.utils.ErrorParser.toErrorResponse;

@RequiredArgsConstructor
@Component
public class ItemClient {
    private static final String ITEM_URI = "/items";
    private final WebClient webClient;

    public ItemDto createItem(ItemCreateDto newItem, int ownerId) {
        try {
            return webClient
                    .post()
                    .uri(ITEM_URI)
                    .header("X-Sharer-User-Id", String.valueOf(ownerId))
                    .bodyValue(newItem)
                    .retrieve()
                    .bodyToMono(ItemDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public ItemDto updateItem(ItemUpdateDto updatedItem, int ownerId, int itemId) {
        try {
            return webClient
                    .patch()
                    .uri(ITEM_URI + "/" + itemId)
                    .header("X-Sharer-User-Id", String.valueOf(ownerId))
                    .bodyValue(updatedItem)
                    .retrieve()
                    .bodyToMono(ItemDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public ItemWithCommentsDto findById(int itemId) {
        try {
            return webClient
                    .get()
                    .uri(ITEM_URI + "/" + itemId)
                    .retrieve()
                    .bodyToMono(ItemWithCommentsDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<ItemOwnerDto> findByOwnerId(int ownerId) {
        try {
            return webClient
                    .get()
                    .uri(ITEM_URI)
                    .header("X-Sharer-User-Id", String.valueOf(ownerId))
                    .retrieve()
                    .bodyToFlux(ItemOwnerDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<ItemDto> search(String text) {
        try {
            return webClient
                    .get()
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(ITEM_URI)
                                    .queryParam("text", text)
                                    .build())
                    .retrieve()
                    .bodyToFlux(ItemDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public CommentDto postComment(CommentCreateDto newComment, int itemId, int ownerId) {
        try {
            return webClient
                    .post()
                    .uri(ITEM_URI + "/" + itemId + "/comment")
                    .bodyValue(newComment)
                    .header("X-Sharer-User-Id", String.valueOf(ownerId))
                    .retrieve()
                    .bodyToMono(CommentDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }

    }

}
