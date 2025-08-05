package ru.practicum.shareit.item;

import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody ItemCreateDto newItem, @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.create(newItem, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemUpdateDto updatedItem, @PathVariable int itemId, @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.update(updatedItem, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsDto findById(@PathVariable int itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemOwnerDto> findItemsByOwnerId(@RequestHeader(name = "X-Sharer-User-Id") int ownerId) {
        return itemService.getItemsWithBookingsAndComments(ownerId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(name = "text") String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody CommentCreateDto newComment,
                                 @PathVariable int itemId,
                                 @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.postComment(newComment, userId, itemId);
    }
}
