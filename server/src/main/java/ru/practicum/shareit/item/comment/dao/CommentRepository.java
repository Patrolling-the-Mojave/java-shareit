package ru.practicum.shareit.item.comment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.comment.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Collection<Comment> findByItemId(int itemId);

    List<Comment> findByItemIdIn(List<Integer> itemIds);
}
