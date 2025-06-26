package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;
import java.util.Collections;

import static ru.practicum.shareit.util.Updater.runIfNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto update(ItemUpdateDto updatedItem, int itemId, int userId) {
        Item oldItem = getItemById(itemId);
        if (oldItem.getOwner().getId() != userId) {
            throw new NoAccessException(String.format("пользователь %d не является владельцем вещи %d", userId, itemId));
        }
        runIfNotNull(updatedItem.getName(), () -> oldItem.setName(updatedItem.getName()));
        runIfNotNull(updatedItem.getDescription(), () -> oldItem.setDescription(updatedItem.getDescription()));
        runIfNotNull(updatedItem.getAvailable(), () -> oldItem.setAvailable(updatedItem.getAvailable()));
        return ItemMapper.toItemDto(itemRepository.update(oldItem));
    }

    @Override
    public Collection<ItemDto> search(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.findItemsByQuery(query)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public Collection<ItemDto> findItemsByOwnerId(int ownerId) {
        return itemRepository.findItemsByOwnerId(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto findById(int itemId) {
        return ItemMapper.toItemDto(getItemById(itemId));
    }

    @Override
    public ItemDto create(ItemCreateDto item, int userId) {
        User owner = getUserById(userId);
        Item newItem = ItemMapper.createItemDtoToItem(item, owner);
        return ItemMapper.toItemDto(itemRepository.create(newItem));
    }

    private Item getItemById(int id) {
        return itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException("предмет с id " + id + " не найден"));
    }

    private User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("пользователь с id " + id + " не найден"));
    }
}
