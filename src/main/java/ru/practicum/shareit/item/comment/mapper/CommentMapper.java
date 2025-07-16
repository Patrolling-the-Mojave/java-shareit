package ru.practicum.shareit.item.comment.mapper;

import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

    public static Set<CommentDto> toCommentDto(Collection<Comment> comments) {
        return comments.stream().map(CommentMapper::toCommentDto).collect(Collectors.toSet());
    }

    public static Comment toEntity(CommentCreateDto newComment, User author, Item item) {
        return new Comment(0, newComment.getText(), author, item, LocalDateTime.now());
    }
}
