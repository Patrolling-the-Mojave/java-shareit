package ru.practicum.shareit.item;

import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Validated @RequestBody ItemCreateDto newItem,
                          @RequestHeader(name = "X-Sharer-User-Id") int ownerId) {
        return itemClient.createItem(newItem, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Validated @RequestBody ItemUpdateDto updatedItem,
                          @PathVariable int itemId,
                          @RequestHeader(name = "X-Sharer-User-Id") int ownerId) {
        return itemClient.updateItem(updatedItem, ownerId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsDto findById(@PathVariable int itemId) {
        return itemClient.findById(itemId);
    }

    @GetMapping
    public Collection<ItemOwnerDto> findItemsByOwnerId(@RequestHeader(name = "X-Sharer-User-Id") int ownerId) {
        return itemClient.findByOwnerId(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(name = "text") String text) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@Validated @RequestBody CommentCreateDto newComment,
                                 @PathVariable int itemId,
                                 @RequestHeader(name = "X-Sharer-User-Id") int ownerId) {
        return itemClient.postComment(newComment, itemId, ownerId);
    }
}
