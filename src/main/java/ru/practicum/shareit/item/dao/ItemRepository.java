package ru.practicum.shareit.item.dao;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    Optional<Item> findById(int id);

    Collection<Item> findItemsByOwnerId(int id);

    Collection<Item> findItemsByQuery(String query);

}
