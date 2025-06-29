package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.Collection;


public interface ItemService {
    ItemDto create(ItemCreateDto newItem, int userId);

    ItemDto update(ItemUpdateDto updatedItem, int itemId, int userId);

    ItemDto findById(int itemId);

    Collection<ItemDto> findItemsByOwnerId(int ownerId);

    Collection<ItemDto> search(String query);
}

