package ru.practicum.shareit.item.service;

import item.*;
import item.comment.CommentCreateDto;
import item.comment.CommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dao.BookingRepository;
import ru.practicum.shareit.exception.NoAccessException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.dao.CommentRepository;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dao.RequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dao.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.item.comment.mapper.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.util.Updater.runIfNotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final RequestRepository requestRepository;

    @Override
    public ItemDto update(ItemUpdateDto updatedItem, int itemId, int userId) {
        Item oldItem = getItemById(itemId);
        if (oldItem.getOwner().getId() != userId) {
            throw new NoAccessException(format("пользователь %d не является владельцем вещи %d", userId, itemId));
        }
        runIfNotNull(updatedItem.getName(), () -> oldItem.setName(updatedItem.getName()));
        runIfNotNull(updatedItem.getDescription(), () -> oldItem.setDescription(updatedItem.getDescription()));
        runIfNotNull(updatedItem.getAvailable(), () -> oldItem.setAvailable(updatedItem.getAvailable()));
        log.trace("вещь с id {} обновлена", itemId);
        log.debug("обновленная вещь {}", oldItem);
        return toDto(itemRepository.save(oldItem));
    }

    @Override
    public Collection<ItemDto> search(String query) {
        log.debug("запрос на поиск по строке " + query);
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        final String lowerCaseQuery = "%" + query.trim().toLowerCase() + "%";
        log.trace("отредактированная строка " + lowerCaseQuery);
        return toDto(itemRepository.searchAvailableItems(lowerCaseQuery));
    }

    @Override
    public ItemWithCommentsDto findById(int itemId) {
        log.debug("поиск вещи с id {}", itemId);
        return toItemWithComments(getItemById(itemId), commentRepository.findByItemId(itemId));
    }

    @Override
    public ItemDto create(ItemCreateDto item, int userId) {
        log.trace("запрос на создание предмета от пользователя {}", userId);
        User owner = getUserById(userId);
        Item newItem = toEntity(item, owner);
        if (item.getRequestId() != null) {
            newItem.setRequest(getRequestById(item.getRequestId()));
        }
        return toDto(itemRepository.save(newItem));
    }

    @Override
    public Collection<ItemOwnerDto> getItemsWithBookingsAndComments(int ownerId) {
        Collection<Item> items = itemRepository.findByOwnerId(ownerId);
        List<Integer> itemIds = items
                .stream()
                .map(Item::getId)
                .toList();
        log.debug("itemIds{}", itemIds);
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);
        log.debug("comments{}", comments);
        Map<Integer, List<Comment>> commentsByItem;

        if (!comments.isEmpty()) {
            commentsByItem = comments
                    .stream()
                    .collect(Collectors.groupingBy(c -> c.getItem().getId()));
        } else {
            commentsByItem = new HashMap<>();
        }
        Map<Integer, Booking> lastBookings = bookingRepository.findLasBookings(itemIds, LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), Function.identity()));
        Map<Integer, Booking> nextBookings = bookingRepository.findNextBookings(itemIds, LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(booking -> booking.getItem().getId(), Function.identity()));
        log.debug("lastBookings{}", lastBookings);
        log.debug("nextBookings{}", nextBookings);
        return items.stream().map(item -> {
                    Booking lastBooking = lastBookings.get(item.getId());
                    LocalDateTime last = lastBooking != null ? lastBooking.getEnd() : null;
                    Booking nextBooking = nextBookings.get(item.getId());
                    LocalDateTime next = nextBooking != null ? nextBooking.getStart() : null;
                    List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    return toItemOwnerDto(item, last, next, itemComments);
                })
                .toList();
    }

    @Override
    public CommentDto postComment(CommentCreateDto newComment, int userId, int itemId) {
        User user = getUserById(userId);
        Item item = getItemById(itemId);
        if (!bookingRepository.existByBookerIdAndItemId(userId, itemId)) {
            throw new IllegalArgumentException(format("клиент %d не пользовался вещью %d", userId, itemId));
        }
        Comment comment = CommentMapper.toEntity(newComment, user, item);
        return toCommentDto(commentRepository.save(comment));
    }

    private Item getItemById(int id) {
        return itemRepository.findById(id).orElseThrow(() ->
                new NotFoundException("предмет с id " + id + " не найден"));
    }

    private User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("пользователь с id " + id + " не найден"));
    }

    private ItemRequest getRequestById(int id) {
        return requestRepository.findById(id).orElseThrow(() ->
                new NotFoundException("запрос с id " + id + " не найден"));
    }
}
