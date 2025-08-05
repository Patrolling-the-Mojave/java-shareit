package ru.practicum.shareit.booking;

import booking.BookingCreateDto;
import booking.BookingDto;
import errors.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.practicum.shareit.exception.GatewayException;

import java.util.Collection;

import static ru.practicum.shareit.utils.ErrorParser.toErrorResponse;

@Component
@RequiredArgsConstructor
public class BookingClient {
    private static final String BOOKING_URI = "/bookings";
    private final WebClient webClient;

    public BookingDto createBooking(BookingCreateDto newBooking, int userId) {
        try {
            return webClient
                    .post()
                    .uri(BOOKING_URI)
                    .header("X-Sharer-User-Id", String.valueOf(userId))
                    .bodyValue(newBooking)
                    .retrieve()
                    .bodyToMono(BookingDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public BookingDto approveBooking(int bookingId, int userId, boolean approved) {
        try {
            return webClient
                    .patch()
                    .uri(uriBuilder -> uriBuilder.path(BOOKING_URI + "/" + bookingId).queryParam("approved", String.valueOf(approved)).build())
                    .header("X-Sharer-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(BookingDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }

    }

    public BookingDto findById(int bookingId, int userId) {
        try {
            return webClient
                    .get()
                    .uri(BOOKING_URI + "/" + bookingId)
                    .header("X-Sharer-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToMono(BookingDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }

    }

    public Collection<BookingDto> findAllByBookerAndState(int userId, String bookingState) {
        try {
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path(BOOKING_URI).queryParam("state", bookingState).build())
                    .header("X-Sharer-User-Id", String.valueOf(userId))
                    .retrieve()
                    .bodyToFlux(BookingDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }

    public Collection<BookingDto> findAllByItemOwnerAndState(int ownerId, String state) {
        try {
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder.path(BOOKING_URI).queryParam("state", state).build())
                    .header("X-Sharer-User-Id", String.valueOf(ownerId))
                    .retrieve()
                    .bodyToFlux(BookingDto.class)
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            final ErrorResponse errorResponse = toErrorResponse(e);
            throw new GatewayException(HttpStatus.valueOf(e.getStatusCode().value()), errorResponse);
        }
    }
}
