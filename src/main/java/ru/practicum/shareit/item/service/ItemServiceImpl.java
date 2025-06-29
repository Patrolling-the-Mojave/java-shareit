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
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.Collection;
import java.util.Collections;

import static ru.practicum.shareit.item.mapper.ItemMapper.toDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.toEntity;
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
        log.trace("вещь с id {} обновлена", itemId);
        log.debug("обновленная вещь {}", oldItem);
        return toDto(itemRepository.update(oldItem));
    }

    @Override
    public Collection<ItemDto> search(String query) {
        log.trace("запрос на поиск по строке");
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return toDto(itemRepository.findItemsByQuery(query));
    }

    @Override
    public Collection<ItemDto> findItemsByOwnerId(int ownerId) {
        log.debug("поиск предметов пользователя с id {}", ownerId);
        return toDto(itemRepository.findItemsByOwnerId(ownerId));
    }

    @Override
    public ItemDto findById(int itemId) {
        log.debug("поиск вещи с id {}", itemId);
        return toDto(getItemById(itemId));
    }

    @Override
    public ItemDto create(ItemCreateDto item, int userId) {
        log.trace("запрос на создание предмета от пользователя {}", userId);
        User owner = getUserById(userId);
        Item newItem = toEntity(item, owner);
        log.debug("новый предмет добавлен {}", newItem);
        return toDto(itemRepository.create(newItem));
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
