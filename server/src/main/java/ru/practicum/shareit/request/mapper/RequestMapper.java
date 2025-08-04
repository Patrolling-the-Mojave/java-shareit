package ru.practicum.shareit.request.mapper;

import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public class RequestMapper {
    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return ItemRequestDto
                .builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequest toEntity(RequestCreateDto newRequest, User user) {
        return new ItemRequest(0, newRequest.getDescription(), user, LocalDateTime.now());
    }

    public static RequestWithAnswersDto toRequestWithAnswers(ItemRequest request, Collection<Item> answers) {
        return RequestWithAnswersDto
                .builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(answers.isEmpty() ? Collections.emptyList() : ItemMapper.toAnswerDto(answers))
                .build();
    }

    public static Collection<ItemRequestDto> toDto(Collection<ItemRequest> requests) {
        return requests.stream().map(RequestMapper::toDto).toList();
    }
}
