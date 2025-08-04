package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import request.ItemRequestDto;
import request.RequestCreateDto;
import request.RequestWithAnswersDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.RequestMapper.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    public ItemRequestDto createRequest(RequestCreateDto newRequest, int requesterId) {
        User user = getUserById(requesterId);
        ItemRequest request = toEntity(newRequest, user);
        return toDto(requestRepository.save(request));
    }

    public Collection<ItemRequestDto> findAll() {
        return toDto(requestRepository.findAllByOrderByCreatedDesc());
    }

    public Collection<RequestWithAnswersDto> findByRequesterId(int requesterId) {
        getUserById(requesterId);
        Collection<ItemRequest> requests = requestRepository.findAllByRequesterId(requesterId);
        List<Integer> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Integer, List<Item>> itemsByRequestId = itemRepository.findAllWhereRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests
                .stream()
                .map(request -> toRequestWithAnswers(request, itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())))
                .toList();
    }

    public RequestWithAnswersDto findByRequestId(int requestId) {
        ItemRequest request = getRequestById(requestId);
        Collection<Item> answers = itemRepository.findAllByRequestId(requestId);
        return toRequestWithAnswers(request, answers);
    }

    private User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("пользователь с id " + id + " не найден"));
    }

    private ItemRequest getRequestById(int requestId) {
        return requestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("запрос с id " + requestId + " не найден"));
    }
}
