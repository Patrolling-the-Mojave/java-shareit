package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
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

    public static ItemWithCommentsDto toItemWithComments(Item item, Collection<Comment> comments) {
        return ItemWithCommentsDto
                .builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(CommentMapper.toCommentDto(comments))
                .build();
    }

    public static ItemShortDto toShortDto(Item item) {
        return new ItemShortDto(item.getId(), item.getName());
    }

    public static ItemOwnerDto toItemOwnerDto(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking, Collection<Comment> comments) {
        return ItemOwnerDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .last(lastBooking)
                .next(nextBooking)
                .comments(CommentMapper.toCommentDto(comments))
                .build();
    }
}
