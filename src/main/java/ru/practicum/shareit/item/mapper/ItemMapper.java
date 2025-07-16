package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collection;

public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();
    }

    public static Collection<ItemDto> toDto(Collection<Item> items) {
        return items.stream()
                .map(ItemMapper::toDto)
                .toList();
    }

    public static Item toEntity(ItemCreateDto createDto, User owner) {
        return new Item(0, createDto.getName(), createDto.getDescription(), owner, null, createDto.getAvailable());
    }
}
