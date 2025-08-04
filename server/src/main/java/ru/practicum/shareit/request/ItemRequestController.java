package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.request.service.RequestService;

import java.util.Collection;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto createRequest(@RequestBody RequestCreateDto newRequest,
                                        @RequestHeader(name = "X-Sharer-User-Id") int requesterId) {
        return requestService.createRequest(newRequest, requesterId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> findAll() {
        return requestService.findAll();
    }

    @GetMapping
    public Collection<RequestWithAnswersDto> findByRequesterId(@RequestHeader(name = "X-Sharer-User-Id") int requesterId) {
        return requestService.findByRequesterId(requesterId);
    }

    @GetMapping("/{requestId}")
    public RequestWithAnswersDto findById(@PathVariable int requestId) {
        return requestService.findByRequestId(requestId);
    }

}
