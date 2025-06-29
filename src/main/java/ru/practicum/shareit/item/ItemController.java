package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Validated @RequestBody ItemCreateDto newItem, @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.create(newItem, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@Validated @RequestBody ItemUpdateDto updatedItem, @PathVariable int itemId, @RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.update(updatedItem, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable int itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> findItemsByOwnerId(@RequestHeader(name = "X-Sharer-User-Id") int userId) {
        return itemService.findItemsByOwnerId(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam(name = "text") String text) {
        return itemService.search(text);
    }
}
