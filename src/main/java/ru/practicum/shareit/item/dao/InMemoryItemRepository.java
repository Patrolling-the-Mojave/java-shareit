package ru.practicum.shareit.item.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, Item> items = new HashMap<>();
    private int currentId = 1;

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> findItemsByOwnerId(int id) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner().getId() == id)
                .toList();
    }

    @Override
    public Optional<Item> findById(int id) {
        Item item = items.get(id);
        if (item == null) {
            return Optional.empty();
        }
        return Optional.of(item);
    }

    @Override
    public Item create(Item item) {
        item.setId(getNewId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> findItemsByQuery(final String query) {
        String lowerCaseQuery = query.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable().equals(Boolean.TRUE))
                .filter(item -> item.getName().toLowerCase().contains(lowerCaseQuery) || item.getDescription().toLowerCase().contains(lowerCaseQuery))
                .toList();
    }

    private int getNewId() {
        if (!items.containsKey(currentId)) {
            return currentId;
        }
        currentId++;
        return getNewId();
    }
}
