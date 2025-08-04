package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;

import java.util.Collection;

@RequiredArgsConstructor
@RestController
@RequestMapping("/requests")
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ItemRequestDto createRequest(@Validated @RequestBody RequestCreateDto newRequest, @RequestHeader(name = "X-Sharer-User-Id") int requesterId) {
        return requestClient.createRequest(newRequest, requesterId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> findAll() {
        return requestClient.findAll();
    }

    @GetMapping
    public Collection<RequestWithAnswersDto> findByRequesterId(@RequestHeader(name = "X-Sharer-User-Id") int requesterId) {
        return requestClient.findByRequesterId(requesterId);
    }

    @GetMapping("/{requestId}")
    public RequestWithAnswersDto findById(@PathVariable int requestId) {
        return requestClient.findById(requestId);
    }
}
