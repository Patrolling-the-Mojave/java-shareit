package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.*;

import java.util.Collection;


public interface ItemService {
    ItemDto create(ItemCreateDto newItem, int userId);

    ItemDto update(ItemUpdateDto updatedItem, int itemId, int userId);

    ItemWithCommentsDto findById(int itemId);

    Collection<ItemDto> search(String query);

    Collection<ItemOwnerDto> getItemsWithBookingsAndComments(int ownerId);

    CommentDto postComment(CommentCreateDto newComment, int userId, int itemId);

}

